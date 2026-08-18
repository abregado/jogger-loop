package com.abregado.joggerloop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * A manually picked palette, not automatic Material You / system dark-mode detection -
 * deliberately, so someone with low vision or light sensitivity mid-workout can pick a
 * scheme that actually works for them rather than whatever the OS wallpaper produced.
 * Persisted by name (see AppSettings.colorScheme) - the data/service layers stay
 * Compose-free and only store the raw name; this enum is where it resolves to a real
 * Material3 ColorScheme, at the UI layer.
 */
enum class AppColorScheme(val label: String, val swatch: Color, val scheme: ColorScheme) {
    LIGHT(
        label = "Light",
        swatch = Color(0xFF0D9488),
        scheme = lightColorScheme(
            primary = Color(0xFF0D9488),
            onPrimary = Color.White,
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF334155),
            error = Color(0xFFDC2626),
            onError = Color.White,
        ),
    ),
    DARK(
        label = "Dark",
        swatch = Color(0xFF2DD4BF),
        scheme = darkColorScheme(
            primary = Color(0xFF2DD4BF),
            onPrimary = Color(0xFF00332E),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF0F172A),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFFCBD5E1),
            error = Color(0xFFF87171),
            onError = Color(0xFF450A0A),
        ),
    ),
    HIGH_CONTRAST(
        label = "High Contrast",
        swatch = Color(0xFFFFD400),
        scheme = darkColorScheme(
            primary = Color(0xFFFFD400),
            onPrimary = Color(0xFF000000),
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFFFFFFFF),
            error = Color(0xFFFF453A),
            onError = Color(0xFF000000),
        ),
    ),
    MUTED(
        label = "Muted",
        swatch = Color(0xFF7C9C93),
        scheme = lightColorScheme(
            primary = Color(0xFF7C9C93),
            onPrimary = Color.White,
            background = Color(0xFFF5F3F0),
            onBackground = Color(0xFF3F3A36),
            surface = Color(0xFFF5F3F0),
            onSurface = Color(0xFF3F3A36),
            surfaceVariant = Color(0xFFEDEBE7),
            onSurfaceVariant = Color(0xFF5B5551),
            error = Color(0xFFB3543F),
            onError = Color.White,
        ),
    ),
    VIBRANT(
        label = "Vibrant",
        swatch = Color(0xFFFF5A36),
        scheme = darkColorScheme(
            primary = Color(0xFFFF5A36),
            onPrimary = Color(0xFF2B0900),
            background = Color(0xFF161221),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF161221),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF211B33),
            onSurfaceVariant = Color(0xFFE4DEF5),
            error = Color(0xFFFF6B6B),
            onError = Color(0xFF2B0000),
        ),
    ),
    WARM(
        label = "Warm",
        swatch = Color(0xFFC2410C),
        scheme = lightColorScheme(
            primary = Color(0xFFC2410C),
            onPrimary = Color.White,
            background = Color(0xFFFFF4E6),
            onBackground = Color(0xFF4A2C13),
            surface = Color(0xFFFFF4E6),
            onSurface = Color(0xFF4A2C13),
            surfaceVariant = Color(0xFFFDEBD3),
            onSurfaceVariant = Color(0xFF6B4423),
            error = Color(0xFFB91C1C),
            onError = Color.White,
        ),
    ),
    ;

    companion object {
        val Default = DARK

        fun fromName(name: String?): AppColorScheme = entries.find { it.name == name } ?: Default
    }
}
