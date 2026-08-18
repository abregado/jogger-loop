package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
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

@Composable
fun MainScreen(service: TimerService?, modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(TimerServiceState()) }
    LaunchedEffect(service) {
        service?.state?.collect { state = it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ControlPanel(
            state = state,
            onStart = { service?.start() },
            onStop = { service?.stop() },
            onReset = { service?.reset() },
        )

        if (state.timers.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(state.timers, key = { _, timer -> timer.id }) { index, timer ->
                    TimerRow(
                        label = timer.label.ifBlank { "Timer ${index + 1}" },
                        remainingMs = service?.getRemainingMs(index) ?: timer.durationMs,
                        progress = service?.getProgress(index) ?: 0f,
                    )
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
