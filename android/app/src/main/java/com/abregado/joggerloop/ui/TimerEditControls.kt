package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.data.PulseMode
import com.abregado.joggerloop.ui.icons.AppIcons

/** The icon toolbar (tone/vibrate/pulse/delete) shown per timer in edit mode. Renaming
 *  isn't here - tapping the timer's label directly (see TimerRow) triggers it, rather than
 *  spending a button slot on a separate pencil icon. Reordering also lives separately, in
 *  [ReorderControls] - see TimerRow, which places it outside this cluster entirely. */
@Composable
fun TimerEditControls(
    tone: Boolean,
    vibrate: Boolean,
    pulseMode: PulseMode,
    onToggleTone: () -> Unit,
    onToggleVibrate: () -> Unit,
    onSetPulseMode: (PulseMode) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChunkySquareToggleButton(checked = tone, onCheckedChange = { onToggleTone() }) {
            Icon(AppIcons.Tone, contentDescription = "Tone", modifier = Modifier.size(ChunkyIconSize))
        }
        ChunkySquareToggleButton(checked = vibrate, onCheckedChange = { onToggleVibrate() }) {
            Icon(AppIcons.Vibrate, contentDescription = "Vibrate", modifier = Modifier.size(ChunkyIconSize))
        }
        PulseModeToggle(selected = pulseMode, onSelect = onSetPulseMode)
        ChunkySquareButton(onClick = onDelete, contentColor = MaterialTheme.colorScheme.error) {
            Icon(AppIcons.Trash, contentDescription = "Delete timer", modifier = Modifier.size(ChunkyIconSize))
        }
    }
}

/** Move-up/move-down, sized and spaced to sit apart from [TimerEditControls] entirely -
 *  they're destructive-adjacent in effect (reordering a running workout plan) and were
 *  easy to mis-tap when crammed in alongside the icon toolbar at default sizes. */
@Composable
fun ReorderControls(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChunkySquareButton(onClick = onMoveUp, enabled = canMoveUp) {
            Text("↑", style = MaterialTheme.typography.headlineSmall)
        }
        ChunkySquareButton(onClick = onMoveDown, enabled = canMoveDown) {
            Text("↓", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun PulseModeToggle(selected: PulseMode, onSelect: (PulseMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == PulseMode.SINGLE,
            onClick = { onSelect(PulseMode.SINGLE) },
            label = { Text("●", textAlign = TextAlign.Center) },
            modifier = Modifier.height(ChunkyButtonSize),
        )
        FilterChip(
            selected = selected == PulseMode.TRIPLE,
            onClick = { onSelect(PulseMode.TRIPLE) },
            label = { Text("● ● ●", textAlign = TextAlign.Center) },
            modifier = Modifier.height(ChunkyButtonSize),
        )
    }
}
