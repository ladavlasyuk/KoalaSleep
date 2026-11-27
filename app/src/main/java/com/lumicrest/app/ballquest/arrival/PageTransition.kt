package com.lumicrest.app.ballquest.arrival

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.lumicrest.app.ballquest.R

internal fun Activity.showWebsite(startAddress: String) {
    val pageIntent = Intent(this, WebPageActivity::class.java).apply {
        putExtra(WebPageActivity.START_ADDRESS, startAddress)
    }
    startActivity(pageIntent)
    applyPageFade(this, closing = false)
    finish()
    applyPageFade(this, closing = true)
}

@Suppress("DEPRECATION")
private fun applyPageFade(activity: Activity, closing: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val type = if (closing) {
            Activity.OVERRIDE_TRANSITION_CLOSE
        } else {
            Activity.OVERRIDE_TRANSITION_OPEN
        }
        activity.overrideActivityTransition(
            type,
            R.anim.moonleaf_web_fade_in,
            R.anim.moonleaf_web_fade_out
        )
    } else {
        activity.overridePendingTransition(R.anim.moonleaf_web_fade_in, R.anim.moonleaf_web_fade_out)
    }
}
