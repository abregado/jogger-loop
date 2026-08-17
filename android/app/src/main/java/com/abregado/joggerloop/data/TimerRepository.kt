package com.abregado.joggerloop.data

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class TimerRepository(context: Context) {
    private val file: File = File(context.filesDir, "app-state.json")

    fun load(): AppState {
        if (!file.exists()) return AppState.EMPTY
        return try {
            AppState.fromJson(JSONObject(file.readText()))
        } catch (e: JSONException) {
            AppState.EMPTY
        } catch (e: IOException) {
            AppState.EMPTY
        }
    }

    fun save(state: AppState) {
        file.writeText(state.toJson().toString())
    }
}
