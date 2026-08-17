package com.abregado.joggerloop.engine

import com.abregado.joggerloop.data.TimerConfig

/**
 * What a call to [TimerEngine.tick] wants the caller (the foreground service) to do.
 * The engine never fires tone/vibration itself - it only reports what happened, so it
 * stays a pure, framework-free function of its own state.
 */
sealed class TimerEvent {
    /** A single timer completed mid-run; fire that timer's own tone/vibrate settings. */
    data class TimerAlert(val timer: TimerConfig) : TimerEvent()

    /** The very last timer of the very last loop completed - fire the finish alert instead. */
    data object AllLoopsFinished : TimerEvent()
}
