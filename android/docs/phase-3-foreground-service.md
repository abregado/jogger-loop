# Phase 3 — Foreground Service

## Goal

The piece that makes this whole rewrite worthwhile: a `TimerService` that keeps running behind the lock screen, drives the `TimerEngine` from Phase 2, and turns its events into real notification updates, vibration, and tone — all while the screen is off.

## Design

```
TimerService (LifecycleService)
 ├─ owns a TimerEngine instance (built from the current AppState via TimerRepository)
 ├─ startForeground() with a live-updating Notification
 ├─ coroutine loop: tick → handle events → update notification, ~every 100ms while running
 ├─ Vibrator for TimerEvent.TimerAlert / AllLoopsFinished
 ├─ ToneGenerator (or SoundPool) for the same events
 ├─ PARTIAL_WAKE_LOCK held while running, released on stop/reset
 └─ exposes StateFlow<EngineSnapshot> for the UI (Phase 4) to bind to and display
```

## Tasks

1. **Service class**: extend `LifecycleService` (from `androidx.lifecycle:lifecycle-service`) rather than plain `Service` — gives a `lifecycleScope` for coroutines for free, without adding a heavier dependency.

2. **Notification channel**: create once at app startup (`NotificationChannel`, `IMPORTANCE_LOW` — we don't want the system's own heads-up/sound behavior since the app fires its own tone/vibration explicitly), required on API 26+.

3. **`startForeground()`** with a `NotificationCompat.Builder`:
   - `setVisibility(NotificationCompat.VISIBILITY_PUBLIC)` so the content actually shows on the lock screen (the default, `PRIVATE`, hides content there).
   - Content text: current timer label + remaining time. Update on a throttled cadence (e.g. once per second) rather than every 100ms tick — the engine ticks fast for smooth in-app progress, but the notification doesn't need to repaint that often and doing so would just churn the system unnecessarily.

4. **Foreground service type declaration**: Android 14+ (API 34+) requires an explicit `android:foregroundServiceType` in the manifest. None of the predefined categories (`mediaPlayback`, `location`, etc.) cleanly describe "interval timer" — the likely fit is `specialUse`, which requires a short justification string in the manifest. Confirm this against whatever API level ends up as the target SDK once Phase 0 is settled.

5. **Vibration**: `val vibrator = getSystemService(Vibrator::class.java)`, using `VibrationEffect.createWaveform(pattern, -1)` for both the per-timer patterns and the 5-slow-pulse finish signal — port the exact millisecond patterns already tuned in `vibration.js` (`[140,120,140,120,140]` for triple, `550` for single, `[450,350,450,350,450,350,450,350,450]` for the finish alert).

6. **Tone**: `ToneGenerator(AudioManager.STREAM_ALARM, volume).startTone(...)`. Worth flagging as a real behavior difference from the web version up front: `ToneGenerator` offers a fixed set of preset DTMF/supervisory tones rather than arbitrary frequencies the way Web Audio's oscillator did (440/880/988 Hz). Two options once we get here: accept the closest preset tones, or pre-render short WAV files at the exact frequencies we want and play them via `SoundPool` for pixel-perfect parity with the PWA's sound. Decide this when we reach the phase rather than now — it doesn't block anything else.

7. **Partial wake lock**: acquire `PowerManager.PARTIAL_WAKE_LOCK` when the engine starts running, release on stop/reset. This is what keeps the CPU from sleeping mid-countdown even with the screen off — distinct from (and in addition to) the foreground service's own scheduling priority.

8. **Control surface**: handle `Intent` actions `ACTION_START`/`ACTION_STOP`/`ACTION_RESET` in `onStartCommand()`, so both the Activity (Phase 4) and notification action buttons (Phase 6) can drive the service through the same entry point.

9. **State exposure**: a `StateFlow<EngineSnapshot>` (a small data class capturing status, currentIndex, per-timer progress, loopsRemaining) that Phase 4's UI collects while bound.

## Acceptance criteria

- Start a timer from the app, **lock the phone**, and physically wait through a timer boundary — confirm tone and vibration both fire and the notification's remaining-time text has advanced correctly, entirely with the screen off.
- Confirm the service survives at least several minutes locked without being killed (this is the real test of whether the foreground service + wake lock combination is holding).
- Confirm `adb shell dumpsys battery` / Android's own battery usage screen doesn't show anything alarming after a normal-length test run (sanity check against runaway wake-lock or busy-looping).

## Notes / gotchas

- Some OEM Android skins (Samsung, Xiaomi, etc.) apply extra background-process killing beyond stock Android's Doze, foreground service status notwithstanding. If reliability issues show up specifically on one device, the fix is usually the user manually disabling battery optimization for the app in system settings — not something the app itself can force.
- Keep the tick loop's actual work cheap (it already is, per Phase 2's design) — the coroutine `delay(100)` loop running continuously while a timer is active is normal and expected for a foreground service, not something to optimize away.
