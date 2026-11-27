package com.lumicrest.app.ballquest.arrival

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ArrivalScreen(
    settings: LandingPreferences,
    showWebsite: (String) -> Unit,
    enterTrail: () -> Unit
) {
    androidx.activity.compose.BackHandler {}
    val context = LocalContext.current
    val service = remember { LaunchGateway(context.applicationContext) }
    var offline by remember { mutableStateOf(false) }

    var attemptGroup by remember { mutableStateOf(0) }

    LaunchedEffect(attemptGroup) {
        offline = false
        when (val route = settings.resolve(service::readLaunchReply)) {
            is LaunchRoute.Web -> showWebsite(route.address)
            LaunchRoute.Game -> enterTrail()
            LaunchRoute.Retry -> offline = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (offline) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Unable to reach the service",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Check your connection and try again. The service may be temporarily offline.",
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { attemptGroup += 1 }) {
                    Text("Try again")
                }
            }
        } else {
            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
