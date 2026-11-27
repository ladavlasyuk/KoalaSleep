package com.lumicrest.app.ballquest.arrival

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast

internal fun Context.enqueuePageDownload(
    downloadHref: String,
    agent: String,
    disposition: String,
    mimeType: String
) {
    try {
        val request = DownloadManager.Request(Uri.parse(downloadHref))
        request.setMimeType(mimeType)

        val cookies = CookieManager.getInstance().getCookie(downloadHref)
        if (!cookies.isNullOrEmpty()) {
            request.addRequestHeader("Cookie", cookies)
        }
        request.addRequestHeader("User-Agent", agent)

        val fileName = URLUtil.guessFileName(downloadHref, disposition, mimeType)
        request.setTitle(fileName)
        request.setDescription("Downloading file...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Download started: $fileName", Toast.LENGTH_SHORT).show()
    } catch (error: Exception) {
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            android.util.Log.e("MoonleafWeb", "Download request failed", error)
        }
        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
    }
}
