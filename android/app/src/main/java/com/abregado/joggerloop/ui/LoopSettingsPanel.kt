package com.abregado.joggerloop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.ui.icons.AppIcons
import com.abregado.joggerloop.util.formatDuration

/** Loop count stepper + finish-alert tone/vibrate, and the one-loop/all-loops length line - matches the PWA's final merged/centered design. */
@Composable
fun LoopSettingsPanel(
    loopCount: Int,
    finishTone: Boolean,
    finishVibrate: Boolean,
    oneLoopMs: Long,
    allLoopsMs: Long,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onToggleFinishTone: () -> Unit,
    onToggleFinishVibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDecrement, enabled = loopCount > 1) { Text("−") }
            Text(
                text = "$loopCount Loop${if (loopCount == 1) "" else "s"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IconButton(onClick = onIncrement, enabled = loopCount < 99) { Text("+") }

            Row(modifier = Modifier.padding(start = 12.dp)) {
                IconToggleButton(
                    checked = finishTone,
                    onCheckedChange = { onToggleFinishTone() },
                    colors = toggleIconColors(),
                ) {
                    Icon(AppIcons.Tone, contentDescription = "Tone when all loops finish")
                }
                IconToggleButton(
                    checked = finishVibrate,
                    onCheckedChange = { onToggleFinishVibrate() },
                    colors = toggleIconColors(),
                ) {
                    Icon(AppIcons.Vibrate, contentDescription = "Vibrate when all loops finish")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "One loop: ${formatDuration(oneLoopMs)}    All loops: ${formatDuration(allLoopsMs)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
