package com.lumicrest.app.ballquest.arrival

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lumicrest.app.ballquest.MainActivity

class ArrivalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = LandingPreferences(this)
        setContent {
            ArrivalScreen(
                settings = settings,
                showWebsite = ::showWebsite,
                enterTrail = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}
