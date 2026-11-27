package com.lumicrest.app.ballquest

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class Ending {
    Nest,
    Gap,
    Exhausted,
}

data class Leaf(val x: Float, val height: Float)

data class Opening(val start: Float, val end: Float)

class Trail(val index: Int, val name: String, val length: Int, val description: String) {
    val glades = (380 until length - 140 step listOf(980, 1230, 1480)[index]).map(Int::toFloat)
    val gaps = buildList {
        var x = 780
        var count = 0
        while (x < length - 140) {
            if (glades.none { abs(it - x) < 215 }) {
                add(Opening(x.toFloat(), x + 30f + index * 5))
            }
            x += listOf(565, 435, 365)[index] + listOf(0, 95, 40)[count++ % 3]
        }
    }
    val leaves =
        buildList {
                for (x in 145 until length - 90 step listOf(445, 550, 980)[index]) {
                    if (gaps.none { x > it.start - 75 && x < it.end + 70 })
                        add(Leaf(x.toFloat(), 20f))
                }
                if (index > 0) gaps.forEach { add(Leaf((it.start + it.end) / 2, 67f)) }
            }
            .sortedBy { it.x }
}

val trails =
    listOf(
        Trail(0, "Fern Hollow", 6000, "Gentle gaps beneath the ferns"),
        Trail(1, "Firefly Creek", 9000, "Leaf-laden leaps across the creek"),
        Trail(2, "Moonrise Canopy", 12000, "Long stretches between moonlit rests"),
    )

class Run(val trail: Trail) {
    var x = 0f
    var height = 0f
    var velocity = 0f
    var energy = 100f
    var collected = 0
    var perfect = 0
    var lastGlade = -1
    var nextLeaf = 0
    var leafTaken = false
    var sleepTicks = -1
    var holdTicks = -1
    var feedback = ""
    var feedbackTicks = 0
    var ending: Ending? = null
    val score
        get() = collected * 10 + perfect * 50 + if (ending == Ending.Nest) 200 else 0

    val golden
        get() = sleepTicks in 60..86

    val glade
        get() = trail.glades.indexOfFirst { x in it..(it + 105f) }.takeIf { it > lastGlade }

    fun press() {
        if (ending != null || sleepTicks >= 0 || height != 0f || velocity != 0f) return
        if (glade != null) holdTicks = 0 else velocity = 245f
    }

    fun release() {
        if (ending != null) return
        if (sleepTicks >= 0) wake() else if (holdTicks >= 0) velocity = 245f
        holdTicks = -1
    }

    private fun wake() {
        val exact = golden
        if (exact) perfect++
        energy = min(100f, energy + if (exact) 35 else 15)
        feedback = if (exact) "Perfect Wake  +35" else "Rested  +15"
        feedbackTicks = 95
        sleepTicks = -1
    }

    fun tick() {
        if (ending != null) return
        if (feedbackTicks > 0) feedbackTicks--
        if (sleepTicks >= 0) {
            if (++sleepTicks >= 120) wake()
            return
        }
        if (holdTicks >= 0) {
            if (++holdTicks >= 11) {
                lastGlade = glade ?: lastGlade
                holdTicks = -1
                sleepTicks = 0
            }
            return
        }
        x += 100f / 60
        energy = max(0f, energy - 2f / 60)
        if (velocity != 0f || height > 0) {
            height = max(0f, height + velocity / 60)
            velocity = if (height == 0f) 0f else velocity - 540f / 60
        }
        if (height == 0f && trail.gaps.any { x > it.start && x < it.end }) {
            ending = Ending.Gap
            return
        }
        while (nextLeaf < trail.leaves.size && trail.leaves[nextLeaf].x < x - 15) {
            nextLeaf++
            leafTaken = false
        }
        val leaf = trail.leaves.getOrNull(nextLeaf)
        if (
            !leafTaken &&
                leaf != null &&
                abs(x - leaf.x) < 14 &&
                abs(height + 19 - leaf.height) < 24
        ) {
            collected++
            energy = min(100f, energy + 5)
            leafTaken = true
        }
        ending =
            when {
                energy == 0f && height == 0f -> Ending.Exhausted
                x >= trail.length -> Ending.Nest
                else -> null
            }
    }
}
