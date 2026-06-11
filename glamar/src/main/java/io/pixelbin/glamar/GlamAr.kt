// GlamAr.kt
package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import io.pixelbin.glamar.model.ConfigData
import io.pixelbin.glamar.model.GlamAROverrides
import org.json.JSONArray
import org.json.JSONObject

class GlamAr private constructor(val accessKey: String) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GlamAr? = null
        var BASE_URL = "https://cdn.glamar.io/sdk"
        var API_URL = "https://api.pixelbin.io"


        @SuppressLint("SetJavaScriptEnabled")
        fun init(
            context: Context,
            accessKey: String,
            overrides: GlamAROverrides? = null,
            webView: WebView? = null,
            debug: Boolean = false,
        ): GlamAr {
            GlamArLogger.init(debug)

            // 1) Ensure instance exists BEFORE anything that might call getInstance()
            val inst = instance ?: synchronized(this) {
                instance ?: GlamAr(accessKey).also { instance = it }
            }

            // 2) Now it’s safe to prepare the WebView (which eventually calls getInstance())
            GlamArWebViewManager.prepareWebView(
                context = context,
                overrides = overrides,
                providedWebView = webView
            )

            return inst
        }

        fun addEventListener(event: String, callback: (Any?) -> Unit) {
            GlamArEventManager.addEventListener(event, callback)
        }

        fun addEventListeners(vararg listeners: Pair<String, (Any?) -> Unit>) {
            for ((event, callback) in listeners) {
                GlamArEventManager.addEventListener(event, callback)
            }
        }

        fun removeEventListener(event: String) {
            GlamArEventManager.removeEventListener(event)
        }

        fun applyBySku(skuId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
        }
        fun applyByCategory(category: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyByCategory' , payload: '${category}'  }, '*');")
        }

        fun comparison(state: String, skus: List<String>) {
            val payload = JSONObject()
                .put("state", state)
                .put("skus", JSONArray(skus))

            evaluateJavascript("window.parent.postMessage({ type: 'comparison', payload: $payload }, '*');")
        }

        fun onNailColorEvents(options: String? = null, value: Any? = null) {
            val payload = JSONObject().apply {
                options?.let { put("options", it) }
                value?.let { put("value", JSONObject.wrap(it)) }
            }

            evaluateJavascript("window.parent.postMessage({ type: 'nailColor', payload: $payload }, '*');")
        }

        fun configChange(type: String, value: Number) {
            val configData = ConfigData(type = type, value = value)
            val payload = JSONObject()
                .put("type", configData.type)
                .put("value", configData.value)

            evaluateJavascript("window.parent.postMessage({ type: 'onConfigChange', payload: $payload }, '*');")
        }

        fun applyByMultipleConfigData(config: (Any?) -> Unit) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyByMultipleConfigData' , payload: '${config}'  }, '*');")
        }

        fun addedToCart(skuId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'addedToCart',payload:$skuId } , '*');")
        }

        fun addedToWishlist(skuId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'addedToWishlist',payload:$skuId } , '*');")
        }

        fun applyPatternById(patternId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyPatternByID' , payload: { patternId: '${patternId}' } }, '*');")
        }

        fun open(mode: String? = null,imgURL: String? = null ) {
          if (!mode.isNullOrBlank())
              evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview' , payload: { mode:'${mode}', imgURL: '${imgURL}' } }, '*');")
            else
            evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview'}, '*');")
        }


        fun close() {
            evaluateJavascript("window.parent.postMessage({ type: 'closePreview'}, '*');")
        }
        fun back() {
            evaluateJavascript("window.parent.postMessage({ type: 'backPreview'}, '*');")
        }

        fun snapshot() {
            evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
        }

        fun reset() {
            evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
        }

        fun skinAnalysis(options: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'skinAnalysis' , payload: { options: '${options}' }  }, '*');")
        }

        fun eyePD(options: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'eyePD' , payload: { options: '${options}' }  }, '*');")
        }

        fun openUI(name: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'openUi' , payload: { name: '${name}' }  }, '*');")
        }

        private fun evaluateJavascript(script: String) {
            GlamArWebViewManager.evaluateJavascript(script)
        }

        fun getInstance(): GlamAr {
            return instance ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
