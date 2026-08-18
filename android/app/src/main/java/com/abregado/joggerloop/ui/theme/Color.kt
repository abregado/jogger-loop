package com.abregado.joggerloop.ui.theme

import androidx.compose.ui.graphics.Color

// Material3 has no built-in "warning" role (only primary/secondary/tertiary/error) - these
// are a hand-picked amber pair for non-error cautionary banners, e.g. the notifications-off
// warning. Not routed through the dynamic color scheme since that has no warning slot either.
val WarningContainerLight = Color(0xFFFFE0A3)
val OnWarningContainerLight = Color(0xFF4D3800)
val WarningContainerDark = Color(0xFF4D3800)
val OnWarningContainerDark = Color(0xFFFFDDA1)