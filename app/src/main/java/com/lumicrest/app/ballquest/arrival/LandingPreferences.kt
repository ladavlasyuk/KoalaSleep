package com.lumicrest.app.ballquest.arrival

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.landingStore by preferencesDataStore(name = "moonleaf_landing")

internal class LandingPreferences(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.landingStore)
    private val ticketKey = stringPreferencesKey("landing_ticket")
    private val landingKey = stringPreferencesKey("last_landing_address")

    suspend fun resolve(fetch: suspend () -> String?): LaunchRoute {
        storedAddress()?.let { return LaunchRoute.Web(it) }
        val payload = fetch()
        if (payload.isNullOrBlank()) return LaunchRoute.Retry
        val landing = parseLanding(payload) ?: return LaunchRoute.Game
        rememberLanding(landing)
        return LaunchRoute.Web(landing.url)
    }

    private suspend fun storedAddress(): String? {
        val values = store.data.first()
        return values[landingKey]?.takeIf { it.isNotBlank() }
    }

    private suspend fun rememberLanding(landing: LandingPage) {
        store.edit { values ->
            val ticket = landing.ticket
            if (ticket == null) values.remove(ticketKey) else values[ticketKey] = ticket
            values[landingKey] = landing.url
        }
    }
}

internal sealed interface LaunchRoute {
    data class Web(val address: String) : LaunchRoute
    data object Game : LaunchRoute
    data object Retry : LaunchRoute
}
