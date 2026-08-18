package com.abregado.joggerloop.service

import com.abregado.joggerloop.data.TimerConfig
import com.abregado.joggerloop.engine.RunStatus

/**
 * The shape the UI binds to. Deliberately thin - per-timer progress/remaining time are
 * computed on demand via [TimerService.getProgress]/[TimerService.getRemainingMs] rather
 * than duplicated in here, the same way the PWA's renderList.js called into
 * timerEngine.js directly instead of caching progress in the pub-sub state blob.
 */
data class TimerServiceState(
    val status: RunStatus = RunStatus.IDLE,
    val currentIndex: Int = 0,
    val loopsRemaining: Int = 0,
    val timers: List<TimerConfig> = emptyList(),
)
