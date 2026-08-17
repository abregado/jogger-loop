package com.abregado.joggerloop.data

import org.json.JSONObject

data class AppSettings(
    val loopCount: Int = 1,
    val finishTone: Boolean = true,
    val finishVibrate: Boolean = true,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("loopCount", loopCount)
        put("finishTone", finishTone)
        put("finishVibrate", finishVibrate)
    }

    companion object {
        fun fromJson(json: JSONObject): AppSettings = AppSettings(
            loopCount = json.optInt("loopCount", 1),
            finishTone = json.optBoolean("finishTone", true),
            finishVibrate = json.optBoolean("finishVibrate", true),
        )
    }
}
