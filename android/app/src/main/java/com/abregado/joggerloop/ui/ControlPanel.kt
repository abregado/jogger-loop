package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.engine.RunStatus
import com.abregado.joggerloop.service.TimerServiceState
import com.abregado.joggerloop.ui.icons.AppIcons
import com.abregado.joggerloop.util.formatDuration

@Composable
fun ControlPanel(
    state: TimerServiceState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasTimers = state.timers.isNotEmpty()
    val running = state.status == RunStatus.RUNNING

    Row(modifier = modifier.fillMaxWidth()) {
        IconButton(
            onClick = {},
            enabled = false, // Edit mode arrives in Phase 5
        ) {
            Icon(AppIcons.Pencil, contentDescription = "Edit timers")
        }

        Spacer(Modifier.width(12.dp))

        Button(
            onClick = if (running) onStop else onStart,
            enabled = hasTimers,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = if (running) AppIcons.Pause else AppIcons.Play,
                contentDescription = if (running) "Pause" else "Start",
            )
            if (!running) {
                Spacer(Modifier.width(8.dp))
                val totalMs = state.timers.sumOf { it.durationMs } * state.loopCount
                Text(formatDuration(totalMs))
            }
        }

        Spacer(Modifier.width(12.dp))

        IconButton(
            onClick = onReset,
            enabled = hasTimers && state.status != RunStatus.IDLE,
        ) {
            Icon(AppIcons.Replay, contentDescription = "Reset timers")
        }
    }
}
