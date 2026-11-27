package com.lumicrest.app.ballquest

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.isActive

@Composable
fun NightRun(run: Run, paused: Boolean, pause: () -> Unit, finish: () -> Unit) {
    var frame by remember(run) { mutableIntStateOf(0) }
    val onFinish by rememberUpdatedState(finish)
    LaunchedEffect(run, paused) {
        if (paused) return@LaunchedEffect
        var previous = withFrameNanos { it }
        var remainder = 0L
        while (isActive && run.ending == null) {
            val now = withFrameNanos { it }
            remainder += (now - previous).coerceIn(0, 100_000_000)
            previous = now
            while (remainder >= 16_666_667) {
                run.tick()
                remainder -= 16_666_667
            }
            frame++
        }
        if (run.ending != null) onFinish()
    }
    val revision = frame
    val energy = run.energy
    val leaves = run.collected
    val progress = (run.x / run.trail.length * 100).toInt()
    val hint =
        when {
            run.golden -> "Release now"
            run.sleepTicks >= 0 -> "Watch the firefly"
            run.feedbackTicks > 0 -> run.feedback
            run.glade != null -> "Hold to nap"
            run.x < 220 -> "Tap to jump"
            else -> ""
        }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Header(run.trail.name, end = { RoundAction(Mark.Pause, "Pause", pause) })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Glyph(if (energy < 25) Mark.Moon else Mark.Leaf, Sage)
            Spacer(Modifier.width(8.dp))
            Label("Energy", Sage)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f).height(9.dp).clip(CircleShape).background(Panel)) {
                Box(
                    Modifier.fillMaxWidth(energy / 100)
                        .fillMaxHeight()
                        .background(if (energy < 25) Gold else Sage, CircleShape)
                )
            }
            Spacer(Modifier.width(8.dp))
            Copy("${energy.toInt()}%", Paper)
        }
        val gesture =
            Modifier.pointerInput(run, paused) {
                if (!paused)
                    detectTapGestures(
                        onPress = {
                            run.press()
                            if (tryAwaitRelease()) run.release() else run.holdTicks = -1
                        }
                    )
            }
        Box(
            Modifier.weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .then(gesture)
                .semantics {
                    contentDescription = "Night trail"
                    customActions =
                        listOf(
                            CustomAccessibilityAction("Jump") {
                                if (!paused && run.sleepTicks < 0) {
                                    run.press()
                                    run.release()
                                }
                                true
                            },
                            CustomAccessibilityAction("Nap in moonlight") {
                                if (!paused && run.glade != null) run.press()
                                true
                            },
                            CustomAccessibilityAction("Wake up") {
                                if (!paused && run.sleepTicks >= 0) run.release()
                                true
                            },
                        )
                }
        ) {
            TrailScene(run, { revision }, Modifier.fillMaxSize())
            if (hint.isNotEmpty())
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .padding(14.dp)
                        .background(Ink.copy(.82f), CircleShape)
                        .padding(horizontal = 15.dp, vertical = 7.dp)
                ) {
                    Label(hint, Paper)
                }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Glyph(Mark.Leaf, Sage)
            Spacer(Modifier.width(5.dp))
            Copy("$leaves leaves", Paper)
            Spacer(Modifier.weight(1f))
            Copy("$progress% to nest", Mist)
        }
    }
}

@Composable
fun Guide(back: () -> Unit) {
    var demo by remember { mutableStateOf(Run(trails[0]).apply { x = trail.glades.first() + 35 }) }
    var frame by remember { mutableIntStateOf(0) }
    var held by remember { mutableStateOf(false) }
    var glow by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("Hold to practice") }
    LaunchedEffect(held, demo) {
        if (!held) return@LaunchedEffect
        var last = withFrameNanos { it }
        var remaining = 0L
        while (isActive && held) {
            val now = withFrameNanos { it }
            remaining += (now - last).coerceAtMost(100_000_000)
            last = now
            while (remaining >= 16_666_667) {
                demo.tick()
                remaining -= 16_666_667
            }
            frame++
            glow = demo.golden
            if (demo.feedbackTicks > 0) {
                result = "Rested · Try again"
                held = false
            }
        }
    }

    JournalPage {
        Header("Field Guide", back)
        Heading("How to Play", 42)
        listOf(
                Triple(
                    Mark.Leaf,
                    "Tap to jump",
                    "Leap before a gap. Catch eucalyptus leaves for energy.",
                ),
                Triple(
                    Mark.Moon,
                    "Hold in moonlight to nap",
                    "Hold on a golden branch to rest. Your energy stops draining.",
                ),
                Triple(
                    Mark.Star,
                    "Release when the firefly glows",
                    "Watch for the expanding gold ring. A Perfect Wake restores 35 energy.",
                ),
            )
            .forEach { (icon, title, body) ->
                Row(
                    Modifier.fillMaxWidth()
                        .background(Panel, RoundedCornerShape(16.dp))
                        .padding(15.dp)
                ) {
                    Glyph(icon, Gold)
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Heading(title, 20)
                        Copy(body)
                    }
                }
            }
        fun begin() {
            demo =
                Run(trails[0]).apply {
                    x = trail.glades.first() + 35
                    press()
                }
            glow = false
            held = true
        }
        fun releaseDemo() {
            if (!held) return
            demo.release()
            result = if (demo.perfect > 0) "Perfect Wake!" else "Try again"
            held = false
        }
        Box(
            Modifier.fillMaxWidth()
                .height(182.dp)
                .clip(RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            begin()
                            tryAwaitRelease()
                            releaseDemo()
                        }
                    )
                }
                .semantics {
                    contentDescription = "Practice waking the koala"
                    customActions =
                        listOf(
                            CustomAccessibilityAction("Start nap") {
                                begin()
                                true
                            },
                            CustomAccessibilityAction("Wake up") {
                                releaseDemo()
                                true
                            },
                        )
                }
        ) {
            TrailScene(demo, { frame }, Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.TopCenter)
                    .padding(8.dp)
                    .background(Ink.copy(.85f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Label(
                    if (held && glow) "Release now" else if (held) "Watch the firefly" else result
                )
            }
        }
    }
}
