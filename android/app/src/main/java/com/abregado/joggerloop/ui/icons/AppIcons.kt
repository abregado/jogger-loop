package com.abregado.joggerloop.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Hand-defined vectors reusing the exact SVG path data from the PWA's icons.js, via
 * Compose's SVG path-data parser - avoids pulling in material-icons-extended (several MB)
 * for a handful of icons we've already designed.
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
    val Tone: ImageVector by lazy {
        buildIcon(
            "Tone",
            "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.26 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z",
        )
    }
    val Vibrate: ImageVector by lazy {
        buildIcon(
            "Vibrate",
            "M0 15h2V9H0v6zm3 2h2V7H3v10zm19-8v6h2V9h-2zm-3 10h2V7h-2v10zM17.5 3h-11C5.67 3 5 3.67 5 4.5v15c0 .83.67 1.5 1.5 1.5h11c.83 0 1.5-.67 1.5-1.5v-15c0-.83-.67-1.5-1.5-1.5zM17 19H7V5h10v14z",
        )
    }
    val Trash: ImageVector by lazy {
        buildIcon(
            "Trash",
            "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
        )
    }
}
