package com.abregado.joggerloop.engine

import com.abregado.joggerloop.data.TimerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {

    private var now = 0L
    private val clock = { now }

    private fun timer(id: String, durationMs: Long) = TimerConfig(id = id, durationMs = durationMs)

    @Test
    fun `pause then resume preserves exact remaining time`() {
        val engine = TimerEngine(listOf(timer("a", 10_000)), loopCount = 1, clock = clock)
        engine.start()
        now += 4_000
        engine.tick()
        engine.stop()

        val remainingAtPause = engine.getRemainingMs(0)
        assertEquals(6_000L, remainingAtPause)

        // Time passing while paused must not count against the timer.
        now += 60_000
        engine.start()
        assertEquals(remainingAtPause, engine.getRemainingMs(0))
    }

    @Test
    fun `completed timer stays fully progressed while later timers are untouched`() {
        val engine = TimerEngine(listOf(timer("a", 5_000), timer("b", 5_000)), loopCount = 1, clock = clock)
        engine.start()
        now += 5_000
        engine.tick()

        assertEquals(1f, engine.getProgress(0))
        assertEquals(0f, engine.getProgress(1))
        assertEquals(0L, engine.getRemainingMs(0))
        assertEquals(5_000L, engine.getRemainingMs(1))
    }

    @Test
    fun `looping back decrements loopsRemaining and fires a normal alert, not the finish alert`() {
        val a = timer("a", 3_000)
        val engine = TimerEngine(listOf(a), loopCount = 2, clock = clock)
        engine.start()
        assertEquals(1, engine.loopsRemaining)

        now += 3_000
        val events = engine.tick()

        assertEquals(listOf(TimerEvent.TimerAlert(a)), events)
        assertEquals(0, engine.currentIndex)
        assertEquals(0, engine.loopsRemaining)
        assertEquals(RunStatus.RUNNING, engine.status)
    }

    @Test
    fun `true final completion fires exactly one AllLoopsFinished and freezes progress`() {
        val a = timer("a", 3_000)
        val engine = TimerEngine(listOf(a), loopCount = 1, clock = clock)
        engine.start()

        now += 3_000
        val events = engine.tick()

        assertEquals(listOf(TimerEvent.AllLoopsFinished), events)
        assertEquals(RunStatus.FINISHED, engine.status)
        assertEquals(1f, engine.getProgress(0))
        assertEquals(0L, engine.getRemainingMs(0))

        // Further ticks while FINISHED must not replay the finish event.
        now += 500
        val secondTick = engine.tick()
        assertTrue(secondTick.isEmpty())
    }

    @Test
    fun `a single tick cascades through multiple boundaries after a large time gap`() {
        val a = timer("a", 1_000)
        val b = timer("b", 1_000)
        val c = timer("c", 1_000)
        val engine = TimerEngine(listOf(a, b, c), loopCount = 1, clock = clock)
        engine.start()

        // Jump straight past all three timers in one go, as if the process were briefly suspended.
        now += 3_500
        val events = engine.tick()

        assertEquals(listOf(TimerEvent.TimerAlert(a), TimerEvent.TimerAlert(b), TimerEvent.AllLoopsFinished), events)
        assertEquals(RunStatus.FINISHED, engine.status)
    }

    @Test
    fun `cascading through a loop wrap mid-tick still decrements loopsRemaining correctly`() {
        val a = timer("a", 1_000)
        val b = timer("b", 1_000)
        val engine = TimerEngine(listOf(a, b), loopCount = 2, clock = clock)
        engine.start()

        // 3000ms covers: finish a, finish b (end of loop 1, wraps), finish a again (start of loop 2).
        now += 3_000
        val events = engine.tick()

        assertEquals(
            listOf(TimerEvent.TimerAlert(a), TimerEvent.TimerAlert(b), TimerEvent.TimerAlert(a)),
            events,
        )
        assertEquals(0, engine.loopsRemaining)
        assertEquals(RunStatus.RUNNING, engine.status)
        assertEquals(1, engine.currentIndex) // now on timer b of loop 2, freshly started
    }

    @Test
    fun `reset returns to idle with zero progress everywhere`() {
        val engine = TimerEngine(listOf(timer("a", 1_000)), loopCount = 1, clock = clock)
        engine.start()
        now += 500
        engine.tick()
        engine.reset()

        assertEquals(RunStatus.IDLE, engine.status)
        assertEquals(0f, engine.getProgress(0))
        assertEquals(1_000L, engine.getRemainingMs(0))
    }

    @Test
    fun `starting again from FINISHED begins a fresh run`() {
        val engine = TimerEngine(listOf(timer("a", 1_000)), loopCount = 3, clock = clock)
        engine.start()
        now += 1_000
        engine.tick() // loopsRemaining 2 -> 1, wraps
        now += 1_000
        engine.tick() // loopsRemaining 1 -> 0, wraps
        now += 1_000
        engine.tick() // final completion -> FINISHED

        assertEquals(RunStatus.FINISHED, engine.status)

        engine.start()

        assertEquals(RunStatus.RUNNING, engine.status)
        assertEquals(0, engine.currentIndex)
        assertEquals(2, engine.loopsRemaining)
        assertEquals(1_000L, engine.getRemainingMs(0))
    }
}
