package com.abregado.joggerloop.data

import org.json.JSONObject

data class TimerConfig(
    val id: String,
    val label: String = "",
    val durationMs: Long,
    val tone: Boolean = true,
    val vibrate: Boolean = true,
    val pulseMode: PulseMode = PulseMode.SINGLE,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("durationMs", durationMs)
        put("tone", tone)
        put("vibrate", vibrate)
        put("pulseMode", pulseMode.jsonValue)
    }

    companion object {
        fun fromJson(json: JSONObject): TimerConfig = TimerConfig(
            id = json.getString("id"),
            label = json.optString("label", ""),
            durationMs = json.getLong("durationMs"),
            tone = json.optBoolean("tone", true),
            vibrate = json.optBoolean("vibrate", true),
            pulseMode = PulseMode.fromJsonValue(json.optString("pulseMode", PulseMode.SINGLE.jsonValue)),
        )
    }
}
