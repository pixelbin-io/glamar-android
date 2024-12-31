package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout.LayoutParams
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object GlamArWebViewManager {

    private var webView: WebView? = null
    private var glamArCallback: GlamArCallback? = null
    private var pMode: PreviewMode = PreviewMode.None
    private const val GLAM_AR_STAGING_URL = "https://glamarz0.de/sdk/"
    private const val GLAM_AR_PROD_URL = "https://glamarz0.de/sdk/"

    /**
     * Prepare a WebView instance with the given URL
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun prepareWebView(
        context: Context, development: Boolean = true, previewMode: PreviewMode = PreviewMode.None
    ) {
        clearPreparedWebView() // clear old instance if there any
        pMode = previewMode

        webView = WebView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.e("WebView", "onPageFinished: $url")
                    initPreview()
                }
            }

            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun onLog(args: String) {
                    Log.e("GlamAR", "onLog: $args")
                    try {
                        val argsJson = JSONObject(args)
                        val type = argsJson.getString("type")
                        when (type) {
                            "init-complete" -> glamArCallback?.onInitComplete()
                            "loading" -> glamArCallback?.onLoading()
                            "sku-applied" -> glamArCallback?.onSkuApplied()
                            "sku-failed" -> glamArCallback?.onSkuFailed()
                            "photo-loaded" -> {
                                val payload = argsJson.getJSONObject("payload").toMap()
                                glamArCallback?.onPhotoLoaded(payload)
                            }

                            "loaded" -> glamArCallback?.onLoaded(pMode)

                            "error" -> {
                                val errorMessage =
                                    argsJson.optString("message", "Unknown error occurred")
                                glamArCallback?.onError(errorMessage)
                            }

                            "face-analysis" -> {
                                val payload = argsJson.getJSONObject("payload").toMap()
                                glamArCallback?.onFaceAnalysisCompleted(payload)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GlamARView", "Error processing JavaScript message", e)
                        glamArCallback?.onError("Error processing JavaScript message: ${e.message}")
                    }
                }
            }, "Android")

            // Load the URL
            val glamArHostUrl = if (development) GLAM_AR_STAGING_URL else GLAM_AR_PROD_URL
            loadUrl(glamArHostUrl)
        }
    }

    /**
     * Get the prepared WebView instance
     */
    fun getPreparedWebView(): WebView? {
        return webView
    }

    /**
     * Clear the current WebView instance
     */
    fun clearPreparedWebView() {
        webView?.apply {
            clearCache(true)
            clearHistory()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

    /**
     * Set callback for WebView events
     */
    fun setGlamArCallBack(callback: GlamArCallback) {
        glamArCallback = callback
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        return keys().asSequence().associateWith { get(it) }
    }

    /**
     * Evaluate a JavaScript script in the WebView
     */
    fun evaluateJavascript(script: String) {
        Log.d("GlamARView", "Evaluating: outside: $webView")

        webView?.post {
            Log.d("GlamARView", "Evaluating: $script")
            webView?.evaluateJavascript(script) {
                Log.d("GlamARView", "JavaScript evaluation result: $it")
            }
        }
    }

    fun initPreview() {
        val script = when (pMode) {
            is PreviewMode.None -> "window.parent.postMessage({ type: 'initialize', payload: {mode:'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true} }, '*');"
            is PreviewMode.Image -> "window.parent.postMessage({ type: 'initialize', payload: {mode :'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openImageOnInit : '${(pMode as PreviewMode.Image).imageUrl}'} }, '*');"
            is PreviewMode.Camera -> "window.parent.postMessage({ type: 'initialize', payload: {mode :'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openLiveOnInit : true} }, '*');"
            is PreviewMode.FaceAnalysis -> "window.parent.postMessage({ type: 'initialize', payload: {mode :'private', platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', category: 'faceanalysis', disableCrossIcon: true, disablePrevIcon: true, openLiveOnInit : true} }, '*');"
        }
        evaluateJavascript(script)
    }
}