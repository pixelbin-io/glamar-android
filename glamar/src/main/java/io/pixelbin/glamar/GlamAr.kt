// GlamAr.kt
package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import io.pixelbin.glamar.model.GlamAROverrides

class GlamAr private constructor(val accessKey: String) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GlamAr? = null
        var BASE_URL = "https://cdn.glamarz0.de/sdk"

        @SuppressLint("SetJavaScriptEnabled")
        fun init(
            context: Context,
            accessKey: String,
            overrides: GlamAROverrides? = null,
            webView: WebView? = null,
            debug: Boolean = false,
        ): GlamAr {
            GlamArLogger.init(debug)

            GlamArWebViewManager.prepareWebView(
                context = context,
                overrides = overrides,
                providedWebView = webView
            )

            return instance ?: synchronized(this) {
                instance ?: GlamAr(accessKey).also { instance = it }
            }
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

        fun applySku(skuId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
        }
        fun applyByCategory(category: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyByCategory' , payload: '${category}'  }, '*');")
        }
        fun applyByMultipleConfigData(config: (Any?) -> Unit) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyByMultipleConfigData' , payload: '${config}'  }, '*');")
        }



        fun applyPatternId(patternId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyPatternByID' , payload: { patternId: '${patternId}' } }, '*');")
        }

        fun open() {
            evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview'}, '*');")
        }

        fun openUploadMode(imgURl: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview' , payload: { mode:'imgTryOn' ,imgURL: '${imgURl}' } }, '*');")
        }

        fun close() {
            evaluateJavascript("window.parent.postMessage({ type: 'closePreview'}, '*');")
        }

        fun snapshot() {
            evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
        }

        fun reset() {
            evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
        }

        fun comparison(option: String, value: String) {
            val script = """
        window.parent.postMessage({
            type: 'comparison',
            payload: {
                options: '${option}',
                value: '${value}'
            }
        }, '*');
        """.trimIndent()

            evaluateJavascript(script)
        }

        fun skinAnalysis(options: String, category: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'skin-analysis' , payload: { options: '${options}', value:'${category}' }  }, '*');")
        }

        private fun evaluateJavascript(script: String) {
            GlamArWebViewManager.evaluateJavascript(script)
        }

        fun getInstance(): GlamAr {
            return instance ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
