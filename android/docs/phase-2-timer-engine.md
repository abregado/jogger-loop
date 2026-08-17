# Phase 2 — Timer Engine

## Goal

Port `timerEngine.js`'s tick/loop/catch-up logic into a pure Kotlin class — no Android framework dependencies at all, so it's trivially unit-testable with plain JUnit (no emulator, no device needed). This is the piece we already worked hardest to get right in the PWA (pause/resume math, loop wraparound, the "finish alert replaces the final timer's own alert" rule) — the goal here is a faithful translation, not a redesign.

## Design

```kotlin
enum class RunStatus { IDLE, RUNNING, PAUSED }

sealed class TimerEvent {
    data class TimerAlert(val timer: TimerConfig) : TimerEvent()      // per-timer tone/vibrate
    object AllLoopsFinished : TimerEvent()                             // finish tone/vibrate instead
}

class TimerEngine(
    private val timers: List<TimerConfig>,
    private val loopCount: Int,
    private val clock: () -> Long = System::currentTimeMillis,   // injected for testability
) {
    var status = RunStatus.IDLE; private set
    var currentIndex = 0; private set
    private var accumulatedMs = 0L
    private var segmentStartedAt: Long? = null
    var loopsRemaining = 0; private set

    fun start() { ... }
    fun stop() { ... }
    fun reset() { ... }
    fun tick(): List<TimerEvent> { ... }   // returns events for the caller (the Service) to act on
    fun getProgress(index: Int): Float { ... }
    fun getRemainingMs(index: Int): Long { ... }
}
```

The key architectural difference from the JS version: `tick()` **returns** a list of events instead of directly calling `playTone()`/`vibrate()` inline. The engine stays a pure function of its own state — the Service (Phase 3) is responsible for turning `TimerEvent.TimerAlert` into an actual `Vibrator`/`ToneGenerator` call and `TimerEvent.AllLoopsFinished` into the 5-slow-pulse finish signal. This separation is what makes the engine unit-testable without touching any Android API.

## Tasks

1. **Port `start()`/`stop()`/`reset()`** directly from `timerEngine.js` — same semantics: `start()` resumes from `PAUSED` preserving `accumulatedMs`, or begins fresh from `IDLE` (resetting `currentIndex`, `accumulatedMs`, and computing `loopsRemaining = loopCount - 1`). `stop()` folds elapsed time into `accumulatedMs` and moves to `PAUSED`. `reset()` zeroes everything back to `IDLE`.

2. **Port `tick()`**, including the `while (elapsed >= duration)` catch-up loop that handles multiple boundary crossings in one tick (needed for the same reason it was needed in JS — if the engine doesn't get ticked for a while, one call to `tick()` must correctly cascade through however many timers/loops should have completed in that gap). Port the exact rule we landed on: the *final* completion (last timer, last loop) emits `AllLoopsFinished` instead of that timer's own `TimerAlert` — everything else emits `TimerAlert`.

3. **Port `getProgress(index)`/`getRemainingMs(index)`** — same three-way logic (completed timers report progress `1.0`/remaining `0`, the active timer computes live from elapsed time, upcoming timers report `0`/full duration).

4. **Decide the tick cadence**: the JS version used `setInterval` at 100ms from the browser's event loop. Natively, the Service (Phase 3) will drive this via a coroutine loop (`while (isActive) { engine.tick(); delay(100) }`) — the engine itself doesn't know or care how it's driven, which is exactly why keeping it framework-free matters.

## Acceptance criteria

Port the manual test scenarios we validated by hand against the running PWA (via the browser console) into real, permanent JUnit tests — this is a genuine upgrade over how we tested the web version:

- Pause then resume preserves the exact remaining time (no drift from the pause itself).
- A completed timer's `getProgress()` stays `1.0` while later timers are still `0.0` (the "completed timers stay full" rule).
- Looping back to timer 0 correctly decrements `loopsRemaining` and emits a normal `TimerAlert` for that boundary, not `AllLoopsFinished`.
- The true final completion (last timer, last loop) emits exactly one `AllLoopsFinished` and no `TimerAlert` for that same boundary.
- A single `tick()` call after a large injected time gap correctly cascades through multiple timer/loop boundaries in one call (the "catch-up" behavior), firing the right sequence of events.

## Notes

- Inject the clock (`clock: () -> Long`) rather than calling `System.currentTimeMillis()` directly inside the class — this is what makes "a large time gap between two ticks" trivially testable (just advance a fake clock) instead of needing real `Thread.sleep()` calls in tests, which is exactly the kind of test-timing fragility we ran into doing this manually against the live PWA.
