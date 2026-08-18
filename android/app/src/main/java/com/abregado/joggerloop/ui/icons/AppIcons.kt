package com.abregado.joggerloop.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Hand-defined vectors reusing the exact SVG path data from the PWA's icons.js, via
 * Compose's SVG path-data parser - avoids pulling in material-icons-extended (several MB)
 * for a handful of icons we've already designed. Only the ones Phase 4 actually uses;
 * Tone/Vibrate/Trash get added here when Phase 5's per-timer edit controls need them.
 */
private fun buildIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = SolidColor(Color.Black), // overridden by Icon()'s tint at call sites
    ).build()

object AppIcons {
    val Pencil: ImageVector by lazy {
        buildIcon(
            "Pencil",
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
        )
    }
    val Play: ImageVector by lazy { buildIcon("Play", "M8 5v14l11-7z") }
    val Pause: ImageVector by lazy { buildIcon("Pause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z") }
    val Replay: ImageVector by lazy {
        buildIcon(
            "Replay",
            "M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z",
        )
    }
}
