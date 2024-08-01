package io.pixelbin.galmar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
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

class GlamARView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val prodBeautyHost = "https://websdk.glamar.io/"
    private val stagingBeautyHost = "https://websdk.glamarz0.de/"
    private val prodStyleHost = "https://fyndstyleweb.glamar.io/"
    private val stagingStyleHost = "https://fyndstyleweb.glamarz0.de/internal/index.html"

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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("GlamARView", "WebView finished loading: $url")
                initializeSDK()
                disableInternalUI()
            }
        }
    }

    private var defaultCallback: WeakReference<Callback>? = null

    private var accessKey: String = ""
    private var isBeauty: Boolean = false
    private var staging: Boolean = false
    private var previewMode: PreviewMode = PreviewMode.NONE
    private var imageUrl: String? = null

    private var isInitialized = false
    private var isOpened = false

    init {
        addView(webView)
    }

    fun initialize(
        accessKey: String = this.accessKey,
        isBeauty: Boolean = this.isBeauty,
        staging: Boolean = this.staging,
        previewMode: PreviewMode = this.previewMode,
        imageUrl: String? = this.imageUrl
    ) {
        this.accessKey = accessKey
        this.isBeauty = isBeauty
        this.staging = staging
        this.previewMode = previewMode
        this.imageUrl = imageUrl

        require(accessKey.isNotBlank()) { "Access key must not be blank" }
        require(!(previewMode == PreviewMode.IMAGE && imageUrl.isNullOrBlank())) { "Image URL must be provided for IMAGE preview mode" }

        updateBeautyMode()
        setupWebViewJavaScriptInterface()
        isInitialized = true
        isOpened = false
    }

    private fun checkInitializationAndOpen(action: String, operation: () -> Unit) {
        if (!isInitialized) {
            val error = "GlamARView is not initialized. Call initialize() first."
            Log.e("GlamARView", error)
            defaultCallback?.get()?.onError(error)
            return
        }
        if (!isOpened) {
            val error = "Cannot $action before 'opened' event is received"
            Log.w("GlamARView", error)
            defaultCallback?.get()?.onError(error)
            return
        }
        operation()
    }

    private fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun disableInternalUI() {
        checkInitializationAndOpen("disable internal UI") {
            evaluateJavascript("window.parent.postMessage({ type: 'disableInternalUI'}, '*');")
        }
    }

    private fun initializeSDK() {
        evaluateJavascript("window.parent.postMessage({ type: 'initialize', payload: { platform: 'android', apiKey:'Bearer $accessKey'} }, '*');")
    }

    private fun updateBeautyMode() {
        val host = when {
            staging && isBeauty -> stagingBeautyHost
            staging && !isBeauty -> stagingStyleHost
            !staging && isBeauty -> prodBeautyHost
            else -> prodStyleHost
        }
        loadUrl(host)
    }

    private fun openImagePreview(imgUrl: String) {
        evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview', payload: { mode: 'modelTryOn', imgURL: '$imgUrl' } }, '*');")
    }

    private fun openCameraPreview() {
        evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview'}, '*');")
    }

    fun applySku(skuId: String) {
        checkInitializationAndOpen("apply SKU") {
            evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '$skuId' } }, '*');")
        }
    }

    fun clear() {
        checkInitializationAndOpen("clear SKU") {
            evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
        }
    }

    fun configChange(options: String, value: String? = null) {
        checkInitializationAndOpen("change config") {
            val script = if (value != null) {
                "window.parent.postMessage({ type: 'configChange', payload: { options: '$options', value: '$value' }}, '*');"
            } else {
                "window.parent.postMessage({ type: 'configChange', payload: { options: '$options' }}, '*');"
            }
            evaluateJavascript(script)
        }
    }

    fun snapshot() {
        checkInitializationAndOpen("take snapshot") {
            evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
        }
    }

    fun toggle(showOriginal: Boolean) {
        checkInitializationAndOpen("toggle view") {
            val script = if (showOriginal) {
                "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'show'} }, '*');"
            } else {
                "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'hide'} }, '*');"
            }
            evaluateJavascript(script)
        }
    }

    private fun evaluateJavascript(script: String) {
        webView.post {
            webView.evaluateJavascript(script) {
                Log.d("GlamARView", "JavaScript evaluation result: $it")
            }
        }
    }

    fun setCallback(callback: Callback) {
        this.defaultCallback = WeakReference(callback)
        setupWebViewJavaScriptInterface()
    }

    private fun setupWebViewJavaScriptInterface() {
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onLog(args: String) {
                android.util.Log.e("GlamAR", "onLog: $args", )
                try {
                    val argsJson = JSONObject(args)
                    val type = argsJson.getString("type")
                    when (type) {
                        "init-complete" -> defaultCallback?.get()?.onInitComplete()
                        "loading" -> {
//                            when (previewMode) {
//                                PreviewMode.IMAGE -> imageUrl?.let { openImagePreview(it) }
//                                PreviewMode.CAMERA -> openCameraPreview()
//                                PreviewMode.NONE -> { /* Do nothing */
//                                }
//                            }
                            defaultCallback?.get()?.onLoading()
                        }
                        "sku-applied" -> defaultCallback?.get()?.onSkuApplied()
                        "sku-failed" -> defaultCallback?.get()?.onSkuFailed()
                        "photo-loaded" -> {
                            val payload = argsJson.getJSONObject("payload").toMap()
                            defaultCallback?.get()?.onPhotoLoaded(payload)
                        }

                        "loaded" -> {
                            when (previewMode) {
                                PreviewMode.IMAGE -> imageUrl?.let { openImagePreview(it) }
                                PreviewMode.CAMERA -> openCameraPreview()
                                PreviewMode.NONE -> { /* Do nothing */
                                }
                            }
                            defaultCallback?.get()?.onLoaded()
                        }

                        "opened" -> {
                            isOpened = true
                            defaultCallback?.get()?.onOpened()
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

    enum class PreviewMode {
        NONE,
        IMAGE,
        CAMERA
    }

    private fun handlePermissionRequest(request: PermissionRequest) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                    REQUEST_CODE_PERMISSIONS
                )
                pendingPermissionRequest = request
            }
        } else {
            request.grant(request.resources)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private var pendingPermissionRequest: PermissionRequest? = null
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                pendingPermissionRequest?.grant(pendingPermissionRequest?.resources)
            } else {
                pendingPermissionRequest?.deny()
            }
            pendingPermissionRequest = null
        }
    }
}
