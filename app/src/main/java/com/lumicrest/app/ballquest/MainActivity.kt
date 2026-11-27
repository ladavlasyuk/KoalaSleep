package com.lumicrest.app.ballquest

import android.content.Intent
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.IOException
import kotlinx.coroutines.flow.catch

private val Context.records by preferencesDataStore("moonleaf_records")
private val unlockedKey = intPreferencesKey("unlocked")

private fun bestKey(index: Int) = intPreferencesKey("best_$index")

private val runSaver =
    listSaver<Run, Any>(
        save = {
            listOf(
                it.trail.index,
                it.x,
                it.height,
                it.velocity,
                it.energy,
                it.collected,
                it.perfect,
                it.lastGlade,
                it.nextLeaf,
                it.leafTaken,
                it.sleepTicks,
                it.feedback,
                it.feedbackTicks,
                it.ending?.name ?: "",
            )
        },
        restore = { data ->
            Run(trails[data[0] as Int]).apply {
                x = data[1] as Float
                height = data[2] as Float
                velocity = data[3] as Float
                energy = data[4] as Float
                collected = data[5] as Int
                perfect = data[6] as Int
                lastGlade = data[7] as Int
                nextLeaf = data[8] as Int
                leafTaken = data[9] as Boolean
                sleepTicks = data[10] as Int
                feedback = data[11] as String
                feedbackTicks = data[12] as Int
                ending = (data[13] as String).takeIf { it.isNotEmpty() }?.let(Ending::valueOf)
            }
        },
    )

private enum class Page {
    Home,
    Map,
    Guide,
    Game,
    Results,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent { Moonleaf(savedInstanceState != null) }
    }

    @Composable
    private fun Moonleaf(restored: Boolean) {
        var page by rememberSaveable { mutableStateOf(Page.Home) }
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var run by rememberSaveable(stateSaver = runSaver) { mutableStateOf(Run(trails[0])) }
        var finished by remember(run) { mutableStateOf(run.ending != null) }
        var paused by remember { mutableStateOf(restored && page == Page.Game) }
        var leaving by rememberSaveable { mutableStateOf(false) }
        val store = LocalContext.current.applicationContext.records
        val data by
            remember(store) {
                    store.data.catch {
                        if (it is IOException) emit(emptyPreferences()) else throw it
                    }
                }
                .collectAsState(initial = null)
        val unlocked = (data?.get(unlockedKey) ?: 0).coerceIn(trails.indices)
        fun start(index: Int) {
            selected = index
            run = Run(trails[index])
            leaving = false
            paused = false
            page = Page.Game
        }
        fun back() {
            when {
                leaving -> leaving = false
                page == Page.Game -> {
                    run.holdTicks = -1
                    paused = !paused
                }
                page == Page.Results -> page = Page.Map
                else -> page = Page.Home
            }
        }
        BackHandler { back() }
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE && page == Page.Game) {
                    run.holdTicks = -1
                    paused = true
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
        LaunchedEffect(run, page, finished) {
            if (page == Page.Game && finished) {
                store.edit {
                    it[bestKey(run.trail.index)] =
                        maxOf(it[bestKey(run.trail.index)] ?: 0, run.score)
                    if (run.ending == Ending.Nest)
                        it[unlockedKey] =
                            maxOf(it[unlockedKey] ?: 0, (run.trail.index + 1).coerceAtMost(2))
                }
                page = Page.Results
            }
        }
        MaterialTheme(
            colorScheme = darkColorScheme(primary = Gold, surface = Panel, onSurface = Paper)
        ) {
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xff302938), Ink)))
                    .safeDrawingPadding()
            ) {
                when (page) {
                    Page.Home -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().weight(1f)) {
                            Home(
                                unlocked,
                                {
                                    selected = unlocked
                                    page = Page.Map
                                },
                                { page = Page.Guide },
                            )
                        }
                        TextButton(onClick = { startActivity(Intent(this@MainActivity, PrivacyNoticeActivity::class.java)) }) {
                            Text("Privacy Policy")
                        }
                    }
                    Page.Map ->
                        TrailMap(
                            selected,
                            unlocked,
                            data?.get(bestKey(selected)) ?: 0,
                            { selected = it },
                            { back() },
                            { start(selected) },
                        )
                    Page.Guide -> Guide { back() }
                    Page.Game ->
                        BoxWithConstraints(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.widthIn(max = maxHeight * .95f).fillMaxHeight()) {
                                NightRun(
                                    run,
                                    paused,
                                    {
                                        paused = true
                                        run.holdTicks = -1
                                    },
                                    { finished = true },
                                )
                            }
                        }
                    Page.Results ->
                        Results(
                            run,
                            maxOf(run.score, data?.get(bestKey(run.trail.index)) ?: 0),
                            { start(run.trail.index) },
                            { page = Page.Map },
                            { start(run.trail.index + 1) },
                        )
                }
                if (page == Page.Game && paused) {
                    AlertDialog(
                        onDismissRequest = { back() },
                        containerColor = Panel,
                        shape = RoundedCornerShape(24.dp),
                        title = { Heading(if (leaving) "Leave this run?" else "Paused", 32) },
                        text = {
                            if (leaving) Copy("Your current trail progress will be lost.")
                            else Copy(run.trail.name)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (leaving) {
                                        leaving = false
                                        page = Page.Map
                                    }
                                    paused = false
                                }
                            ) {
                                Text(if (leaving) "Leave Run" else "Resume")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { leaving = !leaving }) {
                                Text(if (leaving) "Keep Playing" else "Trail Map")
                            }
                        },
                    )
                }
            }
        }
    }
}
