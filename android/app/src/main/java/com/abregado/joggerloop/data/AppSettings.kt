package com.abregado.joggerloop.data

import org.json.JSONObject

// colorScheme is the raw name of a com.abregado.joggerloop.ui.theme.AppColorScheme entry,
// not that type directly - this file (and the service layer that owns it) stays Compose-free,
// the same way PulseMode does; only the UI layer resolves the name to an actual ColorScheme.
data class AppSettings(
    val loopCount: Int = 1,
    val finishTone: Boolean = true,
    val finishVibrate: Boolean = true,
    val colorScheme: String = "DARK",
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("loopCount", loopCount)
        put("finishTone", finishTone)
        put("finishVibrate", finishVibrate)
        put("colorScheme", colorScheme)
    }

    companion object {
        fun fromJson(json: JSONObject): AppSettings = AppSettings(
            loopCount = json.optInt("loopCount", 1),
            finishTone = json.optBoolean("finishTone", true),
            finishVibrate = json.optBoolean("finishVibrate", true),
            colorScheme = json.optString("colorScheme", "DARK"),
        )
    }
}
