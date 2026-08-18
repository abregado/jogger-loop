package com.abregado.joggerloop.util

import kotlin.math.ceil

/** Mirrors the PWA's formatMs(): MM:SS, rounding up so a live countdown never shows 00:00 early. */
fun formatDuration(ms: Long): String {
    val totalSeconds = ceil(ms / 1000.0).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// The three below mirror keypadInput.js's digit-shifting duration entry: as the user types
// digits, each new digit pushes in from the right (like a phone number field) rather than
// inserting at a cursor position - proven UX from the PWA, ported as-is rather than
// redesigned around Compose's VisualTransformation.

/** Raw typed digits (up to 4, e.g. "130" for 1:30) -> total milliseconds. */
fun digitsToMs(digits: String): Long {
    val padded = digits.padStart(4, '0').takeLast(4)
    val minutes = padded.substring(0, 2).toLong()
    val seconds = padded.substring(2).toLong().coerceAtMost(59)
    return (minutes * 60 + seconds) * 1000
}

/** Milliseconds -> raw digit string (e.g. 90_000 -> "0130"), the inverse of [digitsToMs]. */
fun msToDigits(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = (totalSeconds / 60).coerceAtMost(99)
    val seconds = totalSeconds % 60
    return "%02d%02d".format(minutes, seconds)
}

/** Raw digit string -> "MM:SS" for display while typing. */
fun formatDigitsAsClock(digits: String): String {
    val padded = digits.padStart(4, '0').takeLast(4)
    return "${padded.substring(0, 2)}:${padded.substring(2)}"
}
