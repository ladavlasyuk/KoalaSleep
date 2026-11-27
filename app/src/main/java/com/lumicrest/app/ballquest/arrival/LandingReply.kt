package com.lumicrest.app.ballquest.arrival

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

internal data class LandingPage(val url: String, val ticket: String? = null)

internal fun parseLanding(payload: String): LandingPage? {
    val body = payload.trimStart()
    if (body.startsWith("{") || body.startsWith("[")) {
        val stream = JSONTokener(body)
        try {
            while (stream.nextClean() != '\u0000') {
                stream.back()
                locateAddress(stream.nextValue())?.let { return LandingPage(it) }
            }
        } catch (_: JSONException) {
            return null
        }
        return null
    }

    val parts = payload.split('#', limit = 2)
    if (parts.size != 2 || parts.any { it.isBlank() }) return null
    return LandingPage(parts[1], parts[0])
}

private fun locateAddress(value: Any?): String? {
    when (value) {
        is JSONObject -> {
            val candidate = (value.opt("rdstr") as? String)?.trim()
            if (candidate != null && candidate.toHttpUrlOrNull() != null) return candidate
            for (key in value.keys()) {
                locateAddress(value.opt(key))?.let { return it }
            }
        }
        is JSONArray -> for (index in 0 until value.length()) {
            locateAddress(value.opt(index))?.let { return it }
        }
    }
    return null
}
