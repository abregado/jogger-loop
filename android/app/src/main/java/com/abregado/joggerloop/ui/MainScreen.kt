package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.service.TimerService
import com.abregado.joggerloop.service.TimerServiceState
import com.abregado.joggerloop.ui.theme.AppColorScheme

@Composable
fun MainScreen(
    service: TimerService?,
    notificationsPermitted: Boolean,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(TimerServiceState()) }
    LaunchedEffect(service) {
        service?.state?.collect { state = it }
    }

    var editMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ControlPanel(
            state = state,
            editMode = editMode,
            onToggleEdit = { editMode = !editMode },
            onStart = { service?.start() },
            onStop = { service?.stop() },
            onReset = { service?.reset() },
        )

        if (editMode) {
            if (!notificationsPermitted) {
                NotificationsDisabledBanner(onOpenSettings = onOpenNotificationSettings)
            }

            val oneLoopMs = state.timers.sumOf { it.durationMs }
            LoopSettingsPanel(
                loopCount = state.loopCount,
                finishTone = state.finishTone,
                finishVibrate = state.finishVibrate,
                oneLoopMs = oneLoopMs,
                allLoopsMs = oneLoopMs * state.loopCount,
                onDecrement = { service?.setLoopCount(state.loopCount - 1) },
                onIncrement = { service?.setLoopCount(state.loopCount + 1) },
                onToggleFinishTone = { service?.setFinishTone(!state.finishTone) },
                onToggleFinishVibrate = { service?.setFinishVibrate(!state.finishVibrate) },
            )
        }

        if (state.timers.isEmpty() && !editMode) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.timers, key = { _, timer -> timer.id }) { index, timer ->
                    TimerRow(
                        timer = timer,
                        index = index,
                        editMode = editMode,
                        remainingMs = service?.getRemainingMs(index) ?: timer.durationMs,
                        progress = service?.getProgress(index) ?: 0f,
                        canMoveUp = index > 0,
                        canMoveDown = index < state.timers.lastIndex,
                        onLabelChange = { newLabel -> service?.updateTimer(timer.id) { it.copy(label = newLabel) } },
                        onDurationChange = { newDurationMs ->
                            service?.updateTimer(timer.id) { it.copy(durationMs = newDurationMs) }
                        },
                        onToggleTone = { service?.updateTimer(timer.id) { it.copy(tone = !it.tone) } },
                        onToggleVibrate = { service?.updateTimer(timer.id) { it.copy(vibrate = !it.vibrate) } },
                        onSetPulseMode = { mode -> service?.updateTimer(timer.id) { it.copy(pulseMode = mode) } },
                        onMoveUp = { service?.moveTimer(timer.id, -1) },
                        onMoveDown = { service?.moveTimer(timer.id, 1) },
                        onDelete = { service?.deleteTimer(timer.id) },
                    )
                }

                if (editMode) {
                    item {
                        OutlinedButton(
                            onClick = { service?.addTimer() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("+ Add Timer")
                        }
                    }
                    item {
                        ColorSchemePicker(
                            selected = AppColorScheme.fromName(state.colorScheme),
                            onSelect = { scheme -> service?.setColorScheme(scheme.name) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "No timers yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
