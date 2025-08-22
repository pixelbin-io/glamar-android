package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout.LayoutParams
import io.pixelbin.glamar.model.Configuration
import io.pixelbin.glamar.model.GlamAROverrides
import org.json.JSONObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object GlamArWebViewManager {

    private var webView: WebView? = null
    private var overRides: GlamAROverrides? = null;
    private var applicationId: String = "";
    private var activityContext: Context? = null;

    /**
     * Prepare a WebView instance with the given URL
     */
    fun prepareWebView(
        context: Context,
        overrides: GlamAROverrides? = null,
        providedWebView: WebView? = null
    ) {
        clearPreparedWebView() // clear old instance if there any
        overRides = overrides;
        applicationId = context.packageName
        GlamArLogger.d("GlamArWebViewManager", "Package ID: $applicationId")

        val actualWebView = providedWebView ?: WebView(context)
        setupWebView(actualWebView)
        webView = actualWebView
    }

    fun setUpActivityContext(context: Context) {
        activityContext = context
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    GlamArLogger.d("WebView", "onPageFinished: $url")
                    initPreview()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    GlamArLogger.d("WebView", "onPermissionRequest: $request")
                    activityContext?.let {
                        GlamArPermissionHandler.handlePermissionRequest(it, request)
                    }
                }
            }

            removeJavascriptInterface("Android")
            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun onLog(args: String) {
                    GlamArLogger.d("WebView", "onLog: $args")
                    try {
                        val argsJson = JSONObject(args)
                        val type = argsJson.getString("type")
                        GlamArLogger.d("WebView", "Event received: $type")
                        GlamArEventManager.dispatchEvent(type, argsJson)
                    } catch (e: Exception) {
                        GlamArLogger.e("WebView", "Error processing JS message", e)
                    }
                }
            }, "Android")

            // Build the URL (as you already had)
            val sdkMetaVersion = (overRides?.meta?.get("sdkVersion") as? String)?.takeIf { it.isNotBlank() }



            val api = GlamArApi(GlamAr.getInstance().accessKey, development = true)

            api.getVersion { result ->
                webView.post {
                    result
                        .onSuccess { sdkVersion ->
                            GlamArLogger.d("GlamArWebViewManager", "Version API done (success: $sdkVersion). Proceeding to loadUrl.")
                            val finalUrl = if (!sdkVersion.isNullOrBlank()) {
                                "${GlamAr.BASE_URL}/v$sdkVersion?"
                            } else {
                                if (sdkMetaVersion != null) {
                                    "${GlamAr.BASE_URL}/v$sdkMetaVersion?"
                                } else {
                                    "${GlamAr.BASE_URL}/v1.0.0?"
                                }

                            }
                            webView.loadUrl(finalUrl)
                        }
                        .onFailure { e ->
                            GlamArLogger.d("GlamArWebViewManager", "Version API failed: ${e.message}. Using fallback.")
                            val finalUrl = if (sdkMetaVersion != null) {
                                "${GlamAr.BASE_URL}/v$sdkMetaVersion?"
                            } else {
                                "${GlamAr.BASE_URL}/v1.0.0?"
                            }
                            webView.loadUrl(finalUrl)
                        }
                }
            }
            // -------------------------------------------------------
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
    private fun clearPreparedWebView() {
        webView?.apply {
            clearCache(true)
            clearHistory()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
        webView = null
        GlamArEventManager.clearAllListeners()
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        return keys().asSequence().associateWith { get(it) }
    }

    /**
     * Evaluate a JavaScript script in the WebView
     */
    fun evaluateJavascript(script: String) {
        GlamArLogger.d("GlamArWebViewManager", "Evaluating: outside: $webView")
        GlamArLogger.d("GlamArWebViewManager", "Evaluating: $script")
        webView?.evaluateJavascript(script) {
            GlamArLogger.d("GlamArWebViewManager", "JavaScript evaluation result: $it")
        }
    }

    fun initPreview() {
        GlamArLogger.d("GlamArWebViewManager", "Init Preview")
        // Initialize the SDK or perform any setup required
        if (overRides == null) {
            evaluateJavascript("window.parent.postMessage({ type: 'initialize', payload: { platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}'}}, '*');");
            return;
        }
        val apiKey = GlamAr.getInstance().accessKey.ifEmpty { "" }
        val platform = "android";

        val payload = mutableMapOf<String, Any>(
            "apiKey" to apiKey,
            "platform" to platform,
            "parentDomain" to applicationId,
        )

        GlamArLogger.d("GlamArWebViewManager", "payload: $payload")

        overRides?.category?.let { payload["category"] = it }

        overRides?.configuration?.let { config: Configuration ->
            val configMap = mutableMapOf<String, Any>()

            config.global?.let { global ->
                val globalMap = mutableMapOf<String, Any>()
                global.openLiveOnInit?.let { globalMap["openLiveOnInit"] = it }
                global.disableClose?.let { globalMap["disableClose"] = it }
                global.disableBack?.let { globalMap["disableBack"] = it }
                if (globalMap.isNotEmpty()) configMap["global"] = globalMap
            }

            config.skinAnalysis?.let { skin ->
                val skinMap = mutableMapOf<String, Any>()
                skin.appId?.let { skinMap["appId"] = it }
                if (skinMap.isNotEmpty()) configMap["skinAnalysis"] = skinMap
            }

            config.ui?.let { ui ->
                val uiMap = mutableMapOf<String, Any>()

                ui.loader?.let { loader ->
                    val loaderMap = mutableMapOf<String, Any>()
                    loader.disable?.let { loaderMap["disable"] = it }
                    loader.jsonData?.let { loaderMap["jsonData"] = it }
                    loader.backgroundColor?.let { loaderMap["backgroundColor"] = it }
                    if (loaderMap.isNotEmpty()) uiMap["loader"] = loaderMap
                }

                ui.watermark?.let { watermark ->
                    val watermarkMap = mutableMapOf<String, Any>()
                    watermark.text?.let { watermarkMap["text"] = it }
                    watermark.fontColor?.let { watermarkMap["fontColor"] = it }
                    watermark.logo?.let { watermarkMap["logo"] = it }
                    if (watermarkMap.isNotEmpty()) uiMap["watermark"] = watermarkMap
                }

                ui.ar?.let { ar ->
                    val arMap = mutableMapOf<String, Any>()
                    ar.disable3DUI?.let { arMap["disable3DUI"] = it }
                    if (arMap.isNotEmpty()) uiMap["ar"] = arMap
                }

                if (uiMap.isNotEmpty()) configMap["ui"] = uiMap
            }

            if (configMap.isNotEmpty()) {
                payload["configuration"] = configMap
            }
            GlamArLogger.d("GlamArWebViewManager", "payload: $payload")
        }

        val jsonPayload = JSONObject(payload as Map<*, *>).toString()
        GlamArLogger.d("GlamArWebViewManager", "jsonPayload: $jsonPayload")

        val sciprt = """
            window.parent.postMessage({
                type: 'initialize',
                payload: $jsonPayload
            }, '*');
        """.trimIndent()
        evaluateJavascript(sciprt);
    }

}
