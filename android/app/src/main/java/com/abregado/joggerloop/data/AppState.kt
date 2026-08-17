package com.abregado.joggerloop.data

import org.json.JSONArray
import org.json.JSONObject

const val CURRENT_SCHEMA_VERSION = 1

data class AppState(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val timers: List<TimerConfig> = emptyList(),
    val settings: AppSettings = AppSettings(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("timers", JSONArray(timers.map { it.toJson() }))
        put("settings", settings.toJson())
    }

    companion object {
        val EMPTY = AppState()

        fun fromJson(json: JSONObject): AppState {
            val migrated = migrate(json)
            val timersJson = migrated.optJSONArray("timers") ?: JSONArray()
            val timers = (0 until timersJson.length()).map {
                TimerConfig.fromJson(timersJson.getJSONObject(it))
            }
            return AppState(
                schemaVersion = migrated.optInt("schemaVersion", CURRENT_SCHEMA_VERSION),
                timers = timers,
                settings = AppSettings.fromJson(migrated.optJSONObject("settings") ?: JSONObject()),
            )
        }
    }
}
