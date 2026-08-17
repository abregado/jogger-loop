package com.abregado.joggerloop.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppStateTest {

    @Test
    fun `round trip through JSON preserves all data`() {
        val original = AppState(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            timers = listOf(
                TimerConfig(
                    id = "a1",
                    label = "Sprint",
                    durationMs = 30_000,
                    tone = true,
                    vibrate = false,
                    pulseMode = PulseMode.TRIPLE,
                ),
                TimerConfig(
                    id = "b2",
                    label = "",
                    durationMs = 15_000,
                    tone = false,
                    vibrate = true,
                    pulseMode = PulseMode.SINGLE,
                ),
            ),
            settings = AppSettings(loopCount = 3, finishTone = false, finishVibrate = true),
        )

        val roundTripped = AppState.fromJson(original.toJson())

        assertEquals(original, roundTripped)
    }

    @Test
    fun `empty state round trips cleanly`() {
        val roundTripped = AppState.fromJson(AppState.EMPTY.toJson())
        assertEquals(AppState.EMPTY, roundTripped)
    }

    @Test
    fun `missing fields default instead of crashing`() {
        // Simulates corrupt or unexpectedly-shaped on-disk data.
        val state = AppState.fromJson(JSONObject())

        assertEquals(CURRENT_SCHEMA_VERSION, state.schemaVersion)
        assertEquals(emptyList<TimerConfig>(), state.timers)
        assertEquals(AppSettings(), state.settings)
    }
}
