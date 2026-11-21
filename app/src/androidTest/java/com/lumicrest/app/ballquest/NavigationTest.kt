package com.lumicrest.app.ballquest

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import org.junit.Rule
import org.junit.Test

class NavigationTest {
    @get:Rule val app = createAndroidComposeRule<MainActivity>()

    private fun back() {
        app.runOnUiThread { app.activity.onBackPressedDispatcher.onBackPressed() }
        app.mainClock.advanceTimeBy(100)
    }

    private fun advanceTo(text: String) {
        repeat(150) {
            if (app.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return
            app.mainClock.advanceTimeBy(96)
        }
        app.onNodeWithText(text).assertExists()
    }

    @Test
    fun homeGuideMapPauseAndExit() {
        back()
        app.onNodeWithText("How to Play").performClick()
        app.onNodeWithText("Tap to jump").assertExists()
        back()
        app.onNodeWithText("Play").performClick()
        app.onNodeWithText("Firefly Creek").performClick()
        if (
            app.onAllNodesWithText("Finish Fern Hollow to unlock")
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
            app.onNodeWithText("Start Run").assertDoesNotExist()
        app.onNodeWithText("Fern Hollow").performClick()
        app.onNodeWithText("Start Run").assertExists()
        app.mainClock.autoAdvance = false
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("Start Run").performScrollTo().performClick()
        app.mainClock.advanceTimeBy(250)
        back()
        app.onNodeWithText("Paused").assertExists()
        app.onNodeWithText("Trail Map").performClick()
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("Leave this run?").assertExists()
        back()
        app.onNodeWithText("Resume").performClick()
        app.mainClock.advanceTimeBy(100)
        app.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        app.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("Paused").assertExists()
        app.onNodeWithText("Trail Map").performClick()
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("Leave Run").performClick()
        app.mainClock.autoAdvance = true
        app.onNodeWithText("Trail Map").assertExists()
        back()
        app.onNodeWithText("Play").assertExists()
    }

    @Test
    fun pauseDuringNapThenFinishAndRetry() {
        app.onNodeWithText("Play").performClick()
        app.onNodeWithText("Fern Hollow").performClick()
        app.onNodeWithText("Start Run").assertExists()
        app.mainClock.autoAdvance = false
        app.onNodeWithText("Start Run").performScrollTo().performClick()
        val scene = app.onNodeWithContentDescription("Night trail")
        advanceTo("HOLD TO NAP")
        scene.performTouchInput { down(center) }
        app.mainClock.advanceTimeBy(500)
        app.onNodeWithText("WATCH THE FIREFLY").assertExists()
        app.activityRule.scenario.recreate()
        app.mainClock.advanceTimeBy(3000)
        app.onNodeWithText("Paused").assertExists()
        app.onNodeWithText("Resume").performClick()
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("WATCH THE FIREFLY").assertExists()
        advanceTo("RELEASE NOW")
        scene.performTouchInput { click(center) }
        app.mainClock.advanceTimeBy(100)
        app.onNodeWithText("PERFECT WAKE  +35").assertExists()
        advanceTo("Missed the Branch")
        app.onNodeWithText("Try Again").performScrollTo().performClick()
        app.mainClock.advanceTimeBy(200)
        scene.assertExists()
        back()
    }

    @Test
    fun completeAdventureAndRestoreRecord() {
        app.onNodeWithText("Play").performClick()
        app.onNodeWithText("Fern Hollow").performClick()
        app.onNodeWithText("Start Run").assertExists()
        app.mainClock.autoAdvance = false
        app.onNodeWithText("Start Run").performScrollTo().performClick()
        val scene = app.onNodeWithContentDescription("Night trail")
        for (trail in trails) {
            var origin = app.mainClock.currentTime
            val moves =
                (trail.glades.map { (it + 45) to true } +
                        trail.gaps.map { (it.start - 26) to false })
                    .sortedBy { it.first }
            for ((position, nap) in moves) {
                val deadline = origin + (position * 10).toLong()
                app.mainClock.advanceTimeBy(deadline - app.mainClock.currentTime)
                if (nap) {
                    scene.performTouchInput { down(center) }
                    val start = app.mainClock.currentTime
                    app.mainClock.advanceTimeBy(1344)
                    scene.performTouchInput { up() }
                    origin += app.mainClock.currentTime - start
                } else scene.performTouchInput { click(center) }
            }
            app.mainClock.advanceTimeBy(
                origin + trail.length * 10L + 300 - app.mainClock.currentTime
            )
            advanceTo("Home Before Dawn")
            if (trail.index < 2) app.onNodeWithText("Next Trail").performScrollTo().performClick()
        }
        app.onNodeWithText("Trail Map").performScrollTo().performClick()
        app.mainClock.advanceTimeBy(150)
        val best =
            app.onNode(hasText(" pts", substring = true))
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
                .first()
                .text
        org.junit.Assert.assertTrue(best.substringBefore(" ").toInt() >= 200)
        app.activityRule.scenario.recreate()
        app.mainClock.autoAdvance = true
        app.onNodeWithText(best).assertExists()
        app.onNodeWithText("Start Run").assertExists()
    }
}
