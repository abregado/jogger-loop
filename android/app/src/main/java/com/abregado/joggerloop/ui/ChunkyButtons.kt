package com.abregado.joggerloop.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

val ChunkyButtonSize = 56.dp
val ChunkyIconSize = 28.dp
val ChunkyShape = RoundedCornerShape(16.dp)

/**
 * A button sitting on a surfaceVariant card needs to stand out from that card, not blend
 * into it - but "lighter" isn't always the right direction: in the dark-oriented color
 * schemes, surfaceVariant is already a step lighter than background (cards glow faintly
 * against a dark canvas), while in the light-oriented ones it's a step darker (cards get a
 * subtle grey tint against white). Extrapolating the same background-to-surfaceVariant step
 * one further multiple (fraction 2f) continues in whichever direction that scheme already
 * chose, so buttons read as "one more level of elevation" correctly in every scheme rather
 * than hardcoding a lighten/darken that would be backwards half the time.
 */
@Composable
fun elevatedButtonContainerColor(): Color = lerp(
    MaterialTheme.colorScheme.background,
    MaterialTheme.colorScheme.surfaceVariant,
    2f,
)

/** A square (rounded-corner), filled-background button - used throughout edit mode instead
 *  of Material's borderless circular IconButton. A visible container reads as a bigger,
 *  more confident target than a bare icon floating in space, and gives every edit-mode
 *  control (icon buttons, +/-, up/down) one consistent shape language. */
@Composable
fun ChunkySquareButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = elevatedButtonContainerColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = ChunkyShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier.size(ChunkyButtonSize),
        content = content,
    )
}

/** Same square shape as [ChunkySquareButton], for a two-state (checked/unchecked) control -
 *  solid primary fill when checked, matching the strong on/off contrast the plain toggle
 *  buttons used to get from custom colors alone. */
@Composable
fun ChunkySquareToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shape = ChunkyShape,
        colors = IconButtonDefaults.filledIconToggleButtonColors(
            containerColor = elevatedButtonContainerColor(),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContainerColor = MaterialTheme.colorScheme.primary,
            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier.size(ChunkyButtonSize),
        content = content,
    )
}
