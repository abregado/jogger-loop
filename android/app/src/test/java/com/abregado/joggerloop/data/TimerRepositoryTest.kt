package com.abregado.joggerloop.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TimerRepositoryTest {

    @Test
    fun `save then load returns an equal state`() {
        val repository = TimerRepository(RuntimeEnvironment.getApplication())

        val state = AppState(
            timers = listOf(TimerConfig(id = "x", label = "Warmup", durationMs = 45_000)),
            settings = AppSettings(loopCount = 2),
        )

        repository.save(state)
        val loaded = repository.load()

        assertEquals(state, loaded)
    }

    @Test
    fun `load with no saved file returns empty state`() {
        val repository = TimerRepository(RuntimeEnvironment.getApplication())
        assertEquals(AppState.EMPTY, repository.load())
    }
}
