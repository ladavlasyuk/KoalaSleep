package com.lumicrest.app.ballquest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.*
import kotlin.math.*

val Ink = Color(0xff181b25)
val Paper = Color(0xffffedcf)
val Gold = Color(0xffe8bd77)
val Sage = Color(0xffa7c5a0)
val Mist = Color(0xffc1b9c9)
val Panel = Color(0xff302d3c)

private val fernStem =
    Path().apply {
        moveTo(0f, 0f)
        quadraticTo(18f, -46f, 0f, -100f)
    }

private val fernLeaves =
    Path().apply {
        for (i in 1..7) {
            val t = i / 8f
            val x = 9f * sin(t * PI).toFloat()
            val y = -100f * t
            for (side in listOf(-1, 1)) {
                val w = (25f - 16f * t) * side
                moveTo(x, y)
                quadraticTo(x + w * 1.1f, y - abs(w) * .15f, x + w, y - abs(w) * 1.22f)
                quadraticTo(x + w * .14f, y - abs(w) * 1.16f, x, y)
                close()
            }
        }
    }

private fun DrawScope.sprig(x: Float, y: Float, length: Float, angle: Float, tint: Color) {
    withTransform({
        translate(x, y)
        rotate(angle, Offset.Zero)
        scale(length / 100f, length / 100f, Offset.Zero)
    }) {
        drawPath(fernStem, tint, style = Stroke(1.5f))
        drawPath(fernLeaves, tint)
    }
}

fun DrawScope.spark(at: Offset, r: Float, tint: Color) {
    drawPath(
        Path().apply {
            moveTo(at.x, at.y - r)
            quadraticTo(at.x + r * .12f, at.y - r * .12f, at.x + r * .72f, at.y)
            quadraticTo(at.x + r * .12f, at.y + r * .12f, at.x, at.y + r)
            quadraticTo(at.x - r * .12f, at.y + r * .12f, at.x - r * .72f, at.y)
            quadraticTo(at.x - r * .12f, at.y - r * .12f, at.x, at.y - r)
            close()
        },
        tint,
    )
}

private fun DrawScope.forest(h: Float, scroll: Float, chapter: Int) {
    val palette = listOf(Color(0xff66517b), Color(0xff505e80), Color(0xff7a5071))
    drawRect(
        Brush.verticalGradient(
            listOf(Color(0xff27223e), palette[chapter], Color(0xffb38283)),
            endY = h,
        ),
        size = Size(400f, h),
    )
    val moon = Offset(263f, h * .30f)
    val r = min(66f, h * .22f)
    drawCircle(
        Brush.radialGradient(listOf(Gold.copy(.24f), Color.Transparent), moon, r * 2.3f),
        r * 2.3f,
        moon,
    )
    drawCircle(
        Brush.linearGradient(
            listOf(Color(0xffffecc1), Color(0xffe5ad83)),
            moon - Offset(r, r),
            moon + Offset(r, r),
        ),
        r,
        moon,
    )
    drawCircle(Gold.copy(.4f), r + 6, moon, style = Stroke(.8f))
    for (i in 0..58) {
        val at = Offset(((i * 71 + 17) % 397).toFloat(), ((i * 131 + 3) % 937) / 937f * h * .74f)
        if ((at - moon).getDistance() < r + 10) continue
        if (i % 7 == 0) spark(at, 3f + i % 4, Paper.copy(.8f))
        else drawCircle(Paper.copy(.58f), .6f + i % 3 * .32f, at)
    }
    for (layer in 0..2) {
        val base = h * (.57f + .13f * layer)
        val tint = listOf(Color(0xff705970), Color(0xff51465e), Color(0xff39364c))[layer]
        val path =
            Path().apply {
                moveTo(-5f, h)
                for (i in -1..81) {
                    val x = i * 5f
                    val phase = x + scroll * (.09f + .09f * layer)
                    val y =
                        base +
                            sin(phase * .019f + layer * 2) * h * .048f +
                            sin(phase * .044f) * h * .018f
                    lineTo(x, y)
                }
                lineTo(405f, h)
                close()
            }
        drawPath(path, tint)
        val shift = (scroll * (.15f + .1f * layer)) % 56
        for (i in -1..9) {
            val x = i * 56f - shift
            val y = base + sin((x + scroll * (.09f + .09f * layer)) * .019f + layer * 2) * h * .048f
            sprig(x, y + 12, 23f + layer * 10, if (i % 2 == 0) -24f else 22f, tint)
        }
    }
    for (side in 0..1) {
        val anchor = if (side == 0) -9f else 409f
        val flip = if (side == 0) 1f else -1f
        withTransform({
            translate(anchor, 0f)
            scale(flip, 1f, Offset.Zero)
        }) {
            val bough =
                Path().apply {
                    moveTo(0f, h * .45f)
                    quadraticTo(13f, h * .20f, 5f, h * .073f)
                    quadraticTo(39f, h * .11f, 76f, h * .027f)
                    quadraticTo(34f, h * .089f, 3f, h * .046f)
                    lineTo(-5f, -1f)
                    close()
                }
            drawPath(bough, Color(0xff353044))
            for (i in 0..4) {
                sprig(
                    13f + i * 11,
                    h * (.064f - i * .004f),
                    27f - i * 2,
                    148f - i * 6,
                    Color(0xff41404e),
                )
            }
        }
    }
    for (i in 0..3) {
        val tint =
            listOf(Color(0xff292c3a), Color(0xff35423e), Color(0xff4b5848), Color(0xff68725b))[i]
        sprig(i * 10f - 12, h + 10, min(122f, h * .5f) - i * 13, 22f + i * 10, tint)
        sprig(411 - i * 8f, h + 12, min(133f, h * .51f) - i * 13, -35f - i * 8, tint)
    }
}

private fun DrawScope.koala(
    image: ImageBitmap,
    x: Float,
    foot: Float,
    side: Float,
    tilt: Float,
    sleepy: Boolean,
) {
    rotate(tilt, Offset(x, foot - side * .42f)) {
        translate(x - side / 2, foot - side) {
            scale(side / 1024f, side / 1024f, Offset.Zero) {
                drawImage(image, dstSize = IntSize(1024, 1024))
                if (sleepy) {
                    for ((eye, angle) in
                        listOf(Offset(490f, 292f) to -13f, Offset(655f, 257f) to -13f)) {
                        rotate(angle, eye) {
                            drawOval(
                                Brush.verticalGradient(
                                    listOf(Color(0xffdfae88), Color(0xffefc39d)),
                                    eye.y - 42,
                                    eye.y + 42,
                                ),
                                eye - Offset(30f, 39f),
                                Size(60f, 78f),
                            )
                            drawPath(
                                Path().apply {
                                    moveTo(eye.x - 26, eye.y)
                                    quadraticTo(eye.x, eye.y + 22, eye.x + 25, eye.y - 2)
                                },
                                Color(0xff543223),
                                style = Stroke(5f, cap = StrokeCap.Round),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.nest(x: Float, y: Float, width: Float) {
    translate(x, y) {
        scale(width / 80f, width / 80f, Offset.Zero) {
            drawOval(Color(0xff503a39), Offset(-40f, -8f), Size(80f, 19f))
            for (i in 0..7) {
                val drop = i % 3 * 2f
                drawPath(
                    Path().apply {
                        moveTo(-41f + i, -9f + drop)
                        quadraticTo(2f - i, 22f - drop, 38f + i % 4, -8f + drop)
                    },
                    if (i % 2 == 0) Color(0xffb38b66) else Color(0xff846248),
                    style = Stroke(2f),
                )
            }
        }
    }
}

@Composable
fun ForestArt(
    modifier: Modifier,
    chapter: Int = 0,
    rest: Boolean = false,
    hero: Boolean = true,
    inNest: Boolean = false,
) {
    val image = ImageBitmap.imageResource(R.drawable.sleeping_koala)
    Canvas(modifier) {
        val s = size.width / 400f
        val h = size.height / s
        scale(s, s, Offset.Zero) {
            forest(h, 0f, chapter)
            if (hero) {
                val side = min(335f, h * .94f)
                drawOval(
                    Color(0xff171924).copy(.45f),
                    Offset(201 - side * .28f, h * .937f),
                    Size(side * .57f, side * .06f),
                )
                koala(image, 196f, h * .96f, side, if (rest) -9f else -3f, rest)
                if (inNest) nest(196f, h * .92f, side * .7f)
            }
            sprig(-6f, h + 15, min(103f, h * .4f), 55f, Color(0xff293732))
            sprig(407f, h + 15, min(100f, h * .4f), -51f, Color(0xff293732))
        }
    }
}

@Composable
fun TrailScene(run: Run, frame: () -> Int, modifier: Modifier) {
    val image = ImageBitmap.imageResource(R.drawable.sleeping_koala)
    Canvas(modifier) {
        frame()
        val s = size.width / 400f
        val h = size.height / s
        val floor = h * .74f
        val camera = run.x - 83f
        scale(s, s, Offset.Zero) {
            forest(h, run.x, run.trail.index)
            fun branch(start: Float, end: Float) {
                val a = max(-50f, start - camera)
                val b = min(450f, end - camera)
                if (b <= a) return
                val shape =
                    Path().apply {
                        moveTo(a, floor)
                        lineTo(b, floor)
                        lineTo(b - 3, floor + 12)
                        quadraticTo(b - 12, floor + 17, b - 23, floor + 17)
                        quadraticTo((a + b) / 2, floor + 23, a + 7, floor + 18)
                        close()
                    }
                drawPath(shape, Color(0xff563c42))
                drawLine(Color(0xffbd906e), Offset(a, floor + 1), Offset(b, floor + 1), 3f)
                drawLine(Color(0xff796051), Offset(a + 6, floor + 7), Offset(b - 6, floor + 8), 2f)
            }
            var start = -500f
            for (gap in run.trail.gaps) {
                if (gap.start > camera + 450) break
                branch(start, gap.start)
                start = gap.end
            }
            branch(start, run.trail.length + 250f)
            for ((i, pos) in run.trail.glades.withIndex()) {
                val local = pos - camera
                if (local !in -125f..405f || i <= run.lastGlade && run.sleepTicks < 0) continue
                val beam =
                    Path().apply {
                        moveTo(local + 80, 0f)
                        lineTo(local + 105, 0f)
                        lineTo(local + 110, floor)
                        lineTo(local, floor)
                        close()
                    }
                drawPath(
                    beam,
                    Brush.verticalGradient(listOf(Gold.copy(.02f), Gold.copy(.27f)), endY = floor),
                )
                drawLine(Gold, Offset(local, floor - 1), Offset(local + 105, floor - 1), 3f)
                spark(Offset(local + 52, floor - 31), 7f, Gold)
            }
            for ((i, leaf) in run.trail.leaves.withIndex()) {
                val x = leaf.x - camera
                if (x > 420) break
                if (x < -12 || i < run.nextLeaf || i == run.nextLeaf && run.leafTaken) continue
                rotate(35f, Offset(x, floor - leaf.height)) {
                    drawOval(
                        Color(0xffbad97a),
                        Offset(x - 6, floor - leaf.height - 10),
                        Size(12f, 20f),
                    )
                    drawLine(
                        Color(0xff667944),
                        Offset(x, floor - leaf.height + 9),
                        Offset(x, floor - leaf.height - 6),
                        1.4f,
                    )
                }
            }
            val nestX = run.trail.length - camera
            if (nestX < 460) {
                sprig(nestX + 10, floor, 84f, 13f, Sage)
                nest(nestX, floor - 8, 74f)
            }
            val asleep = run.sleepTicks >= 0
            val sway = if (asleep) sin(run.sleepTicks * .06f) * 1.5f else sin(run.x * .11f) * 2.2f
            koala(
                image,
                83f,
                floor - run.height + sway,
                80f,
                if (asleep) -12f
                else if (run.height > 0) -6f else sway + if (run.energy < 25) 10 else 0,
                asleep,
            )
            if (asleep) {
                val at = Offset(118f, floor - 52)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Gold.copy(if (run.golden) .85f else .22f), Color.Transparent),
                        at,
                        21f,
                    ),
                    21f,
                    at,
                )
                spark(at, if (run.golden) 9f else 4f, Gold)
                if (run.golden)
                    drawCircle(Gold, 10 + (run.sleepTicks - 60) * .45f, at, style = Stroke(1.5f))
            }
        }
    }
}
