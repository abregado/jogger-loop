package com.abregado.joggerloop.engine

enum class RunStatus {
    IDLE,
    RUNNING,
    PAUSED,

    /**
     * All configured loops just completed. Progress freezes fully at 100% (matching
     * "completed timers stay full") until something external calls [TimerEngine.reset].
     * Distinct from IDLE so a UI/service layer can tell "just finished" from "never started."
     */
    FINISHED,
}
