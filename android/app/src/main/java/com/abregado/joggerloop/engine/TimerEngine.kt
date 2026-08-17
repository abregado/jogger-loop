package com.abregado.joggerloop.engine

import com.abregado.joggerloop.data.TimerConfig

/**
 * Pure, framework-free port of the PWA's timerEngine.js. Holds no timers/threads of its
 * own - a caller (the foreground service) drives it by calling [tick] periodically and
 * acting on the returned [TimerEvent]s.
 *
 * [clock] is injected so tests can move time forward instantly instead of sleeping.
 */
class TimerEngine(
    private val timers: List<TimerConfig>,
    private val loopCount: Int,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    var status: RunStatus = RunStatus.IDLE
        private set
    var currentIndex: Int = 0
        private set
    var loopsRemaining: Int = 0
        private set

    private var accumulatedMs: Long = 0L
    private var segmentStartedAt: Long? = null

    fun start() {
        if (timers.isEmpty()) return
        if (status == RunStatus.IDLE || status == RunStatus.FINISHED) {
            currentIndex = 0
            accumulatedMs = 0
            loopsRemaining = (loopCount - 1).coerceAtLeast(0)
        }
        status = RunStatus.RUNNING
        segmentStartedAt = clock()
    }

    fun stop() {
        if (status != RunStatus.RUNNING) return
        accumulatedMs += clock() - (segmentStartedAt ?: clock())
        segmentStartedAt = null
        status = RunStatus.PAUSED
    }

    fun reset() {
        status = RunStatus.IDLE
        currentIndex = 0
        accumulatedMs = 0
        segmentStartedAt = null
        loopsRemaining = 0
    }

    /** Progress in [0, 1] for the given timer index, for rendering a fill. */
    fun getProgress(index: Int): Float {
        if (status == RunStatus.IDLE) return 0f
        if (status == RunStatus.FINISHED) return 1f
        if (index < currentIndex) return 1f
        if (index > currentIndex) return 0f
        val timer = timers.getOrNull(index) ?: return 1f
        if (timer.durationMs <= 0) return 1f
        return (currentElapsedMs().toFloat() / timer.durationMs).coerceAtMost(1f)
    }

    fun getRemainingMs(index: Int): Long {
        val timer = timers.getOrNull(index) ?: return 0L
        if (status == RunStatus.FINISHED) return 0L
        if (status == RunStatus.IDLE || index > currentIndex) return timer.durationMs
        if (index < currentIndex) return 0L
        return (timer.durationMs - currentElapsedMs()).coerceAtLeast(0L)
    }

    private fun currentElapsedMs(): Long {
        val base = accumulatedMs
        val startedAt = segmentStartedAt
        return if (status == RunStatus.RUNNING && startedAt != null) {
            base + (clock() - startedAt)
        } else {
            base
        }
    }

    /**
     * Advances the engine to "now" and returns whatever [TimerEvent]s occurred along the
     * way. A single call can return multiple events if it's been long enough since the
     * last tick to cross more than one timer/loop boundary (e.g. after the process was
     * briefly suspended) - every crossing still fires its alert, since (unlike the PWA)
     * this engine has no reason to suppress alerts for being "backgrounded."
     *
     * No-op once [status] is [RunStatus.FINISHED]: the finish alert already fired for
     * this run, and repeated calls during the frozen display window must not replay it.
     */
    fun tick(): List<TimerEvent> {
        if (status != RunStatus.RUNNING) return emptyList()

        if (currentIndex !in timers.indices) {
            reset()
            return emptyList()
        }

        var timer: TimerConfig = timers[currentIndex]
        var elapsed = currentElapsedMs()
        val events = mutableListOf<TimerEvent>()

        while (elapsed >= timer.durationMs) {
            val isLast = currentIndex >= timers.lastIndex
            val isFinalCompletion = isLast && loopsRemaining <= 0

            events += if (isFinalCompletion) TimerEvent.AllLoopsFinished else TimerEvent.TimerAlert(timer)

            if (isLast) {
                if (loopsRemaining > 0) {
                    loopsRemaining -= 1
                    elapsed -= timer.durationMs
                    currentIndex = 0
                    accumulatedMs = 0
                    segmentStartedAt = clock() - elapsed
                    timer = timers[0]
                    continue
                }

                status = RunStatus.FINISHED
                return events
            }

            elapsed -= timer.durationMs
            currentIndex += 1
            accumulatedMs = 0
            segmentStartedAt = clock() - elapsed
            timer = timers[currentIndex]
        }

        return events
    }
}
