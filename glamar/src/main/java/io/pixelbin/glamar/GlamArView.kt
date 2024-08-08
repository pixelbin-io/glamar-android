// GlamArView.kt

package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.lang.ref.WeakReference

class GlamArView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val prodBeautyHost = "https://websdk.glamar.io/"
    private val stagingBeautyHost = "https://websdk.glamarz0.de/"
    private val prodStyleHost = "https://fyndstyleweb.glamar.io/"
    private val stagingStyleHost = "https://fyndstyleweb.glamarz0.de/internal/index.html"
    private var defaultCallback: WeakReference<Callback>? = null
    private var previewMode: PreviewMode = PreviewMode.None
    private var isBeauty: Boolean = false
    private var skuApplied: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    private val webView: WebView = WebView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        settings.javaScriptEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        webViewClient = WebViewClient()
        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                handlePermissionRequest(request)
            }
        }
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("WebView", "onPageStarted: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.e("WebView", "onPageFinished: $url")
                init()
            }
        }
    }

    private fun init() {
        GlamAr.getInstance()
        Log.d("reloadPage", previewMode.toString())
        val script = when (previewMode) {
            is PreviewMode.None -> "window.parent.postMessage({ type: 'initialize', payload: {mode:'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true} }, '*');"
            is PreviewMode.Image -> "window.parent.postMessage({ type: 'initialize', payload: {mode :'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openImageOnInit : '${(previewMode as PreviewMode.Image).imageUrl}'} }, '*');"
            is PreviewMode.Camera -> "window.parent.postMessage({ type: 'initialize', payload: {mode :'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openLiveOnInit : true} }, '*');"
        }
        evaluateJavascript(script)
    }

    init {
        addView(webView)
        reloadPage()
        setupWebViewJavaScriptInterface()
    }

    fun startPreview(
        previewMode: PreviewMode = this.previewMode,
        isBeauty: Boolean = this.isBeauty
    ) {
        this.previewMode = previewMode
        if (this.isBeauty != isBeauty) {
            reloadPage(isBeauty)
        }
        this.isBeauty = isBeauty
    }

    private fun reloadPage(isBeauty: Boolean = false) {
        this.isBeauty = isBeauty
        val host = when {
            GlamAr.getInstance().development && this.isBeauty -> stagingBeautyHost
            GlamAr.getInstance().development && !this.isBeauty -> stagingStyleHost
            !GlamAr.getInstance().development && this.isBeauty -> prodBeautyHost
            else -> prodStyleHost
        }
        webView.loadUrl(host)
        Log.d("reloadPage", host)
    }

    fun applySku(skuId: String, category: String) {
        if (category.contains("beauty") != isBeauty) {
            reloadPage(category.contains("beauty"))
        }
        this.skuApplied = skuId
        evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
    }

    fun clear() {
        evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
    }

    fun configChange(options: String, value: Double? = null) {
        val script = if (value != null) {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options', value: '$value' }}, '*');"
        } else {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options' }}, '*');"
        }
        evaluateJavascript(script)
    }

    fun snapshot() {
        evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
    }

    fun toggle(showOriginal: Boolean) {
        val script = if (showOriginal) {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'show'} }, '*');"
        } else {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'hide'} }, '*');"
        }
        evaluateJavascript(script)
    }

    private fun evaluateJavascript(script: String) {
        webView.post {
            Log.d("GlamARView", "Evaluating: $script")
            webView.evaluateJavascript(script) {
                Log.d("GlamARView", "JavaScript evaluation result: $it")
            }
        }
    }

    private fun setupWebViewJavaScriptInterface() {
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onLog(args: String) {
                android.util.Log.e("GlamAR", "onLog: $args")
                try {
                    val argsJson = JSONObject(args)
                    val type = argsJson.getString("type")
                    when (type) {
                        "init-complete" -> defaultCallback?.get()?.onInitComplete()
                        "loading" -> defaultCallback?.get()?.onLoading()
                        "sku-applied" -> defaultCallback?.get()?.onSkuApplied()
                        "sku-failed" -> defaultCallback?.get()?.onSkuFailed()
                        "photo-loaded" -> {
                            val payload = argsJson.getJSONObject("payload").toMap()
                            defaultCallback?.get()?.onPhotoLoaded(payload)
                        }

                        "loaded" -> {
                            if (skuApplied.isNotBlank())
                                evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuApplied}' } }, '*');")
                            defaultCallback?.get()?.onLoaded()
                        }

                        "error" -> {
                            val errorMessage =
                                argsJson.optString("message", "Unknown error occurred")
                            defaultCallback?.get()?.onError(errorMessage)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GlamARView", "Error processing JavaScript message", e)
                    defaultCallback?.get()
                        ?.onError("Error processing JavaScript message: ${e.message}")
                }
            }
        }, "Android")
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        return keys().asSequence().associateWith { get(it) }
    }

    fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        defaultCallback = null
    }

    private fun handlePermissionRequest(request: PermissionRequest) {
        val permissions = request.resources.mapNotNull {
            when (it) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> android.Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> android.Manifest.permission.RECORD_AUDIO
                else -> null
            }
        }.toTypedArray()

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            request.grant(request.resources)
        } else {
            ActivityCompat.requestPermissions(
                (context as Activity),
                missingPermissions.toTypedArray(),
                requestCodePermissions
            )
            pendingPermissionRequest = request
        }
    }

    private val requestCodePermissions = 1001
    private var pendingPermissionRequest: PermissionRequest? = null

    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        if (requestCode == requestCodePermissions) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                pendingPermissionRequest?.grant(pendingPermissionRequest?.resources)
                init()
            } else {
                pendingPermissionRequest?.deny()
            }
            pendingPermissionRequest = null
        }
    }
}

// Callback interface for WebView events
interface Callback {
    fun onInitComplete()
    fun onLoading()
    fun onSkuApplied()
    fun onSkuFailed()
    fun onPhotoLoaded(payload: Map<String, Any>)
    fun onLoaded()
    fun onOpened()
    fun onError(message: String)
}

sealed class PreviewMode {
    data object None : PreviewMode()
    data class Image(val imageUrl: String) : PreviewMode()
    data object Camera : PreviewMode()
}
