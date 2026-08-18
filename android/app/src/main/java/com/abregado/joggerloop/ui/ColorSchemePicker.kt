package com.abregado.joggerloop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.abregado.joggerloop.ui.theme.AppColorScheme

private val SwatchSize = 48.dp

/** Row of tappable swatches switching the app's color scheme - see AppColorScheme for why
 *  this is a manual picker rather than automatic Material You / dark-mode detection. */
@Composable
fun ColorSchemePicker(
    selected: AppColorScheme,
    onSelect: (AppColorScheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        AppColorScheme.entries.forEach { scheme ->
            ColorSchemeSwatch(
                scheme = scheme,
                isSelected = scheme == selected,
                onClick = { onSelect(scheme) },
            )
        }
    }
}

@Composable
private fun ColorSchemeSwatch(scheme: AppColorScheme, isSelected: Boolean, onClick: () -> Unit) {
    val markColor = if (scheme.swatch.luminance() > 0.5f) Color.Black else Color.White
    Box(
        modifier = Modifier
            .size(SwatchSize)
            .clip(CircleShape)
            .background(scheme.swatch)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) markColor else Color.Black.copy(alpha = 0.15f),
                shape = CircleShape,
            )
            .semantics(mergeDescendants = true) { contentDescription = scheme.label }
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Text("✓", color = markColor)
        }
    }
}
