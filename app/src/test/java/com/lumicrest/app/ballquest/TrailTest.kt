package com.lumicrest.app.ballquest

import org.junit.Assert.*
import org.junit.Test

class TrailTest {
    private fun nap(): Run =
        Run(trails.first()).apply {
            x = trail.glades.first()
            energy = 40f
            press()
            repeat(11) { tick() }
            assertEquals(0, sleepTicks)
        }

    @Test
    fun goldenWindowAndOneRewardPerGlade() {
        for (time in listOf(59, 60, 86, 87)) {
            val run = nap()
            repeat(time) { run.tick() }
            run.release()
            run.release()
            val perfect = time in 60..86
            assertEquals(if (perfect) 75f else 55f, run.energy, 0.001f)
            assertEquals(if (perfect) 1 else 0, run.perfect)
            assertNull(run.glade)
        }
    }

    @Test
    fun automaticWakeAndTapOnGlade() {
        val run = nap()
        repeat(120) { run.tick() }
        assertEquals(-1, run.sleepTicks)
        assertEquals(55f, run.energy, 0.001f)
        val tap =
            Run(trails[0]).apply {
                x = trail.glades.first()
                press()
                tick()
                release()
                tick()
            }
        assertTrue(tap.height > 0)
    }

    @Test
    fun endingsAndLeafCollection() {
        val tired =
            Run(trails.first()).apply {
                energy = .001f
                tick()
            }
        assertEquals(Ending.Exhausted, tired.ending)
        val fall =
            Run(trails.first()).apply {
                x = trail.gaps.first().start
                tick()
            }
        assertEquals(Ending.Gap, fall.ending)
        val leaf =
            Run(trails.first()).apply {
                x = trail.leaves.first().x
                energy = 80f
            }
        repeat(6) { leaf.tick() }
        assertEquals(1, leaf.collected)
        assertTrue(leaf.energy in 84f..85f)
    }

    @Test
    fun everyRouteCanBeCompletedWithTheActualInputRules() {
        trails.forEach { trail ->
            val run = Run(trail)
            repeat(9000) {
                if (run.golden) run.release()
                else if (run.glade != null && run.holdTicks < 0) run.press()
                else if (trail.gaps.any { it.start - run.x in 21f..24f }) {
                    run.press()
                    run.release()
                }
                run.tick()
            }
            assertEquals(trail.name, Ending.Nest, run.ending)
            assertTrue(run.perfect > 0)
            assertTrue(run.collected > 0)
            val score = run.collected * 10 + run.perfect * 50 + 200
            run.tick()
            assertEquals(score, run.score)
        }
    }
}
