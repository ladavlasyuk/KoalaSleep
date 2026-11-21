package com.lumicrest.app.ballquest

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

enum class Mark {
    Moon,
    Book,
    Arrow,
    Back,
    Leaf,
    Lock,
    Play,
    Pause,
    Star,
    Map,
}

@Composable
fun Glyph(mark: Mark, color: Color, modifier: Modifier = Modifier.size(21.dp)) {
    Canvas(modifier) {
        val unit = size.minDimension / 24
        scale(unit, unit, Offset.Zero) {
            fun line(x: Int, y: Int, u: Int, v: Int) =
                drawLine(
                    color,
                    Offset(x.toFloat(), y.toFloat()),
                    Offset(u.toFloat(), v.toFloat()),
                    1.65f,
                    StrokeCap.Round,
                )
            val p = Path()
            when (mark) {
                Mark.Moon -> {
                    p.moveTo(16f, 2f)
                    p.cubicTo(-1f, 2f, 2f, 27f, 21f, 18f)
                    p.cubicTo(11f, 18f, 8f, 9f, 16f, 2f)
                    drawPath(p, color)
                    spark(Offset(20f, 7f), 3f, color)
                }
                Mark.Book -> {
                    p.moveTo(12f, 6f)
                    p.quadraticTo(7f, 2f, 2f, 5f)
                    p.lineTo(2f, 19f)
                    p.quadraticTo(7f, 16f, 12f, 20f)
                    p.quadraticTo(17f, 16f, 22f, 19f)
                    p.lineTo(22f, 5f)
                    p.quadraticTo(17f, 2f, 12f, 6f)
                    p.close()
                    drawPath(p, color, style = Stroke(1.6f))
                    line(12, 6, 12, 20)
                }
                Mark.Arrow,
                Mark.Back ->
                    rotate(if (mark == Mark.Back) 180f else 0f, Offset(12f, 12f)) {
                        line(3, 12, 20, 12)
                        line(14, 6, 20, 12)
                        line(14, 18, 20, 12)
                    }
                Mark.Leaf -> {
                    p.moveTo(3f, 21f)
                    p.quadraticTo(0f, 5f, 22f, 2f)
                    p.quadraticTo(22f, 20f, 3f, 21f)
                    drawPath(p, color, style = Stroke(1.7f))
                    line(3, 21, 16, 9)
                }
                Mark.Lock -> {
                    drawRoundRect(
                        color,
                        Offset(5f, 10f),
                        Size(14f, 11f),
                        CornerRadius(2f),
                        style = Stroke(1.7f),
                    )
                    drawArc(
                        color,
                        180f,
                        180f,
                        false,
                        Offset(8f, 3f),
                        Size(8f, 14f),
                        style = Stroke(1.7f),
                    )
                    line(12, 14, 12, 17)
                }
                Mark.Play -> {
                    p.moveTo(7f, 3f)
                    p.lineTo(21f, 12f)
                    p.lineTo(7f, 21f)
                    p.close()
                    drawPath(p, color)
                }
                Mark.Pause -> {
                    drawRoundRect(color, Offset(5f, 4f), Size(5f, 16f), CornerRadius(1f))
                    drawRoundRect(color, Offset(14f, 4f), Size(5f, 16f), CornerRadius(1f))
                }
                Mark.Star -> spark(Offset(12f, 12f), 11f, color)
                Mark.Map -> {
                    p.moveTo(2f, 6f)
                    p.lineTo(8f, 3f)
                    p.lineTo(16f, 6f)
                    p.lineTo(22f, 3f)
                    p.lineTo(22f, 18f)
                    p.lineTo(16f, 21f)
                    p.lineTo(8f, 18f)
                    p.lineTo(2f, 21f)
                    p.close()
                    drawPath(p, color, style = Stroke(1.6f))
                    line(8, 3, 8, 18)
                    line(16, 6, 16, 21)
                }
            }
        }
    }
}

@Composable
fun Heading(value: String, size: Int = 36) {
    Text(
        value,
        color = Paper,
        fontFamily = FontFamily.Serif,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        letterSpacing = (-1).sp,
    )
}

@Composable
fun Label(value: String, color: Color = Gold) {
    Text(
        value.uppercase(java.util.Locale.ENGLISH),
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.7.sp,
    )
}

@Composable
fun Copy(value: String, color: Color = Mist) {
    Text(value, color = color, fontSize = 15.sp, lineHeight = 21.sp)
}

@Composable
fun RoundAction(icon: Mark, name: String, action: () -> Unit) {
    Box(
        Modifier.size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = action)
            .semantics { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(38.dp).border(1.dp, Gold.copy(.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Glyph(icon, Paper, Modifier.size(17.dp))
        }
    }
}

@Composable
fun Header(title: String, onBack: (() -> Unit)? = null, end: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack == null) Glyph(Mark.Moon, Gold, Modifier.size(25.dp))
        else RoundAction(Mark.Back, "Back", onBack)
        Spacer(Modifier.width(8.dp))
        Label(title, Paper)
        Spacer(Modifier.weight(1f))
        end?.invoke()
    }
}

@Composable
fun Action(text: String, icon: Mark, primary: Boolean = true, action: () -> Unit) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(if (primary) Gold else Panel)
            .border(1.dp, if (primary) Color(0xfff5d498) else Gold.copy(.32f), shape)
            .clickable(role = Role.Button, onClick = action)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val fg = if (primary) Color(0xff322b28) else Paper
        Glyph(icon, fg, Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun JournalPage(centered: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 580.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 22.dp, vertical = 12.dp),
            verticalArrangement =
                Arrangement.spacedBy(
                    13.dp,
                    if (centered) Alignment.CenterVertically else Alignment.Top,
                ),
            content = content,
        )
    }
}

@Composable
fun Home(unlocked: Int, map: () -> Unit, guide: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val artHeight = (maxHeight - 310.dp).coerceIn(215.dp, 520.dp)
        JournalPage(centered = true) {
            Header("The Moonleaf Trail", end = { RoundAction(Mark.Map, "Trail Map", map) })
            Text(
                buildAnnotatedString {
                    append("Koala ")
                    withStyle(SpanStyle(color = Gold, fontStyle = FontStyle.Italic)) {
                        append("Sleep")
                    }
                },
                color = Paper,
                fontFamily = FontFamily.Serif,
                fontSize = 57.sp,
                lineHeight = 63.sp,
                letterSpacing = (-2.5).sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            val arch =
                RoundedCornerShape(
                    topStart = 90.dp,
                    topEnd = 90.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                )
            Box(
                Modifier.fillMaxWidth()
                    .height(artHeight)
                    .clip(arch)
                    .border(1.dp, Gold.copy(.68f), arch)
            ) {
                ForestArt(Modifier.fillMaxSize().padding(2.dp).clip(arch))
                Row(
                    Modifier.align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Ink.copy(.84f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Glyph(Mark.Leaf, Sage, Modifier.size(13.dp))
                    Spacer(Modifier.width(6.dp))
                    Label(trails[unlocked].name, Paper)
                }
            }
            Action("Play", Mark.Play, action = map)
            Action("How to Play", Mark.Book, false, guide)
        }
    }
}

@Composable
fun TrailMap(
    selected: Int,
    unlocked: Int,
    best: Int,
    choose: (Int) -> Unit,
    back: () -> Unit,
    start: () -> Unit,
) {
    JournalPage {
        Header("The Moonleaf Trail", back)
        Heading("Trail Map", 43)
        Column(
            Modifier.fillMaxWidth().drawBehind {
                val x = 42.dp.toPx()
                drawLine(Gold.copy(.6f), Offset(x, 20f), Offset(x, size.height - 20f), 3.dp.toPx())
            },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (trail in trails) {
                val active = trail.index == selected
                val locked = trail.index > unlocked
                val shape = RoundedCornerShape(18.dp)
                Row(
                    Modifier.fillMaxWidth()
                        .clip(shape)
                        .background(if (active) Color(0xff3c3744) else Panel)
                        .border(1.dp, if (active) Gold else Color.White.copy(.12f), shape)
                        .clickable(role = Role.Button) { choose(trail.index) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(61.dp).clip(RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        ForestArt(Modifier.fillMaxSize(), trail.index, hero = false)
                        if (locked) Glyph(Mark.Lock, Paper, Modifier.size(22.dp))
                    }
                    Column(
                        Modifier.weight(1f).padding(start = 13.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Label("Trail 0${trail.index+1}", if (locked) Mist else Gold)
                        Text(
                            trail.name,
                            color = Paper,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                        )
                        Copy(
                            if (locked) "Finish ${trails[trail.index-1].name} to unlock"
                            else trail.description
                        )
                    }
                }
            }
        }
        if (selected <= unlocked) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Label("Personal best")
                Copy("$best pts", Paper)
            }
            Action("Start Run", Mark.Play, action = start)
        }
    }
}

@Composable
fun Results(run: Run, best: Int, again: () -> Unit, map: () -> Unit, next: () -> Unit) {
    val won = run.ending == Ending.Nest
    JournalPage {
        Header("Run Results", map)
        Label(run.trail.name)
        Heading(
            when (run.ending) {
                Ending.Nest -> "Home Before Dawn"
                Ending.Gap -> "Missed the Branch"
                else -> "Too Sleepy"
            }
        )
        ForestArt(
            Modifier.fillMaxWidth().height(155.dp).clip(RoundedCornerShape(18.dp)),
            run.trail.index,
            rest = run.ending != Ending.Gap,
            inNest = won,
        )
        Row(
            Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            listOf("Score" to run.score, "Leaves" to run.collected, "Perfect wakes" to run.perfect)
                .forEach { (title, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Heading(value.toString(), 25)
                        Copy(title)
                    }
                }
        }
        Copy("Personal best  ·  $best pts", Gold)
        if (won && run.trail.index < 2) Action("Next Trail", Mark.Arrow, action = next)
        Action("Try Again", Mark.Play, !won || run.trail.index == 2, again)
        Action("Trail Map", Mark.Map, false, map)
    }
}
