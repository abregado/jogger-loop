package com.abregado.joggerloop.util

import kotlin.math.ceil

/** Mirrors the PWA's formatMs(): MM:SS, rounding up so a live countdown never shows 00:00 early. */
fun formatDuration(ms: Long): String {
    val totalSeconds = ceil(ms / 1000.0).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
