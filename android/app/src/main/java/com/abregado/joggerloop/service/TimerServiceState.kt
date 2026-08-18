package com.abregado.joggerloop.service

import com.abregado.joggerloop.data.TimerConfig
import com.abregado.joggerloop.engine.RunStatus

/**
 * The shape the UI binds to. Deliberately thin - per-timer progress/remaining time are
 * computed on demand via [TimerService.getProgress]/[TimerService.getRemainingMs] rather
 * than duplicated in here, the same way the PWA's renderList.js called into
 * timerEngine.js directly instead of caching progress in the pub-sub state blob.
 *
 * [updatedAtMs] exists purely so every publish is structurally distinct: StateFlow drops
 * consecutive equal values, and without this, most ~100ms ticks would be silently
 * deduplicated away since status/currentIndex/etc. only change at timer boundaries - the
 * live progress in between lives in the engine's clock, not in this snapshot.
 */
data class TimerServiceState(
    val status: RunStatus = RunStatus.IDLE,
    val currentIndex: Int = 0,
    val loopsRemaining: Int = 0,
    val loopCount: Int = 1,
    val finishTone: Boolean = true,
    val finishVibrate: Boolean = true,
    val timers: List<TimerConfig> = emptyList(),
    val updatedAtMs: Long = 0L,
)
