package com.lumicrest.app.ballquest.arrival

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.lumicrest.app.ballquest.R

class WebPageActivity : ComponentActivity() {

    companion object {
        const val START_ADDRESS = "moonleaf_start_address"
        private const val FILE_GATEWAY_CODE = 4829
        private const val DOWNLOAD_PERMISSION_CODE = 4830
    }

    private lateinit var pageView: WebView
    private lateinit var loadingIndicator: ProgressBar
    private var pickerCallback: ValueCallback<Array<Uri>>? = null
    private var awaitingFirstPage = true
    private data class PendingDownload(val href: String, val agent: String, val disposition: String, val mime: String)
    private var queuedDownload: PendingDownload? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val initialUrl = intent.getStringExtra(START_ADDRESS) ?: run {
            finish()
            return
        }

        createPageLayout()
        attachPageClients()
        loadInitialPage(initialUrl)
        bindBackStack()
    }

    private fun createPageLayout() {
        val windowColor = ContextCompat.getColor(this, R.color.moonleaf_web_activity_background)
        val pageColor = ContextCompat.getColor(this, R.color.moonleaf_web_surface_bg)
        val indicatorColor = ContextCompat.getColor(this, R.color.moonleaf_web_loader_color)

        val container = RelativeLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(windowColor)
        }

        pageView = WebView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
            setBackgroundColor(pageColor)
        }

        loadingIndicator = ProgressBar(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT)
            }
            indeterminateTintList = ColorStateList.valueOf(indicatorColor)
            visibility = View.VISIBLE
        }

        container.addView(pageView)
        container.addView(loadingIndicator)
        setContentView(container)

        applyImmersiveChrome()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun attachPageClients() {
        pageView.configureForLanding()

        pageView.setDownloadListener { href, agent, disposition, mime, _ ->
            requestDownload(href, agent, disposition, mime)
        }

        pageView.webViewClient = navigationClient()
        pageView.webChromeClient = documentClient()
    }

    private fun navigationClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (awaitingFirstPage) {
                    loadingIndicator.visibility = View.VISIBLE
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (awaitingFirstPage) {
                    awaitingFirstPage = false
                    loadingIndicator.visibility = View.GONE
                }
                CookieManager.getInstance().flush()
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                CookieManager.getInstance().flush()
                super.doUpdateVisitedHistory(view, url, isReload)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme ?: return false

                if (scheme in listOf("http", "https")) {
                    return false
                }

                return try {
                    val externalIntent = if (scheme == "intent") {
                        Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                    } else {
                        Intent(Intent.ACTION_VIEW, uri)
                    }

                    openExternalTarget(view?.context ?: return true, externalIntent)
                    true
                } catch (_: Exception) {
                    true
                }
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                if (!isFinishing && !isDestroyed) {
                    recreate()
                }
                return true
            }
        }
    }

    private fun documentClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                pickerCallback?.onReceiveValue(null)
                pickerCallback = callback

                val acceptedFormats = params?.acceptTypes ?: arrayOf("*/*")
                val mimeType = acceptedFormats.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "*/*"

                val documentPicker = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = mimeType
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                return try {
                    @Suppress("DEPRECATION")
                    startActivityForResult(documentPicker, FILE_GATEWAY_CODE)
                    true
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this@WebPageActivity, "File manager not found", Toast.LENGTH_SHORT).show()
                    callback?.onReceiveValue(null)
                    pickerCallback = null
                    false
                }
            }
        }
    }

    private fun loadInitialPage(href: String) {
        pageView.loadUrl(href)
    }

    private fun bindBackStack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pageView.canGoBack()) {
                    pageView.goBack()
                }
            }
        })
    }

    private fun requestDownload(href: String, agent: String, disposition: String, mime: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            queuedDownload = PendingDownload(href, agent, disposition, mime)
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), DOWNLOAD_PERMISSION_CODE)
            return
        }
        enqueuePageDownload(href, agent, disposition, mime)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DOWNLOAD_PERMISSION_CODE) {
            val pending = queuedDownload
            queuedDownload = null
            if (pending != null && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                enqueuePageDownload(pending.href, pending.agent, pending.disposition, pending.mime)
            } else {
                Toast.makeText(this, "Storage permission is needed to save this file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openExternalTarget(context: Context, intent: Intent): Boolean {
        return try {
            if (context !is ComponentActivity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun applyImmersiveChrome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_GATEWAY_CODE) {
            absorbPickerResult(resultCode, data)
        }
    }

    private fun absorbPickerResult(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val selectedFiles = mutableListOf<Uri>()

            data?.data?.let { uri -> selectedFiles.add(uri) }

            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { uri -> selectedFiles.add(uri) }
                }
            }

            pickerCallback?.onReceiveValue(selectedFiles.toTypedArray())
        } else {
            pickerCallback?.onReceiveValue(null)
        }

        pickerCallback = null
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }
}
