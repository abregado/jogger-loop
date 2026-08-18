package com.abregado.joggerloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun JoggerloopTheme(colorScheme: AppColorScheme = AppColorScheme.Default, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme.scheme, typography = Typography, content = content)
}
