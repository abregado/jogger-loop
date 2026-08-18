package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.data.PulseMode
import com.abregado.joggerloop.ui.icons.AppIcons

/** The icon toolbar (rename/tone/vibrate/pulse/delete) + move up/down column shown per timer in edit mode. */
@Composable
fun TimerEditControls(
    tone: Boolean,
    vibrate: Boolean,
    pulseMode: PulseMode,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRename: () -> Unit,
    onToggleTone: () -> Unit,
    onToggleVibrate: () -> Unit,
    onSetPulseMode: (PulseMode) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRename) {
                Icon(AppIcons.Pencil, contentDescription = "Rename timer")
            }
            IconToggleButton(checked = tone, onCheckedChange = { onToggleTone() }, colors = toggleIconColors()) {
                Icon(AppIcons.Tone, contentDescription = "Tone")
            }
            IconToggleButton(checked = vibrate, onCheckedChange = { onToggleVibrate() }, colors = toggleIconColors()) {
                Icon(AppIcons.Vibrate, contentDescription = "Vibrate")
            }
            PulseModeToggle(selected = pulseMode, onSelect = onSetPulseMode)
            IconButton(onClick = onDelete) {
                Icon(AppIcons.Trash, contentDescription = "Delete timer", tint = MaterialTheme.colorScheme.error)
            }
        }
        Column {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) { Text("↑") }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) { Text("↓") }
        }
    }
}

/** Strong on/off contrast (solid filled background when checked) - Material3's default
 *  IconToggleButton colors were too subtle to read as "on" vs "off" at a glance. */
@Composable
internal fun toggleIconColors() = IconButtonDefaults.iconToggleButtonColors(
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    checkedContainerColor = MaterialTheme.colorScheme.primary,
    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
private fun PulseModeToggle(selected: PulseMode, onSelect: (PulseMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FilterChip(
            selected = selected == PulseMode.SINGLE,
            onClick = { onSelect(PulseMode.SINGLE) },
            label = { Text("●") },
        )
        FilterChip(
            selected = selected == PulseMode.TRIPLE,
            onClick = { onSelect(PulseMode.TRIPLE) },
            label = { Text("● ● ●") },
        )
    }
}
