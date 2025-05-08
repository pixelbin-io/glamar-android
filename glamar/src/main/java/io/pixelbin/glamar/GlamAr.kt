// GlamAr.kt
package io.pixelbin.glamar

import io.pixelbin.glamar.model.GlamAROverrides
import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView

class GlamAr private constructor(val accessKey: String, val debug: Boolean = true) {
    val api: GlamArApi = GlamArApi(accessKey, debug)
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GlamAr? = null
        var BASE_URL = ""
        private const val DEV_URL = "https://api.pixelbinz0.de"
        private const val PROD_URL = "https://api.pixelbin.io"
        @SuppressLint("SetJavaScriptEnabled")
        fun init(
            context: Context,
            accessKey: String,
            debug: Boolean = true,
            overrides: GlamAROverrides? = null,
            webView: WebView? = null
        ): GlamAr {
            GlamArLogger.init(debug)

            GlamArWebViewManager.prepareWebView(
                context = context,
                development = debug,
                overrides = overrides,
                providedWebView = webView
            )

            return instance ?: synchronized(this) {
                BASE_URL = if (debug) DEV_URL else PROD_URL
                instance ?: GlamAr(accessKey, debug = debug).also { instance = it }
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

        fun applyPatternId(patternId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyPatternByID' , payload: { patternId: '${patternId}' } }, '*');")
        }

        fun open(){
            evaluateJavascript("window.parent.postMessage({ type: 'openLivePreview'}, '*');")
        }

        fun openUploadMode(imgURl:String){
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

        fun skinAnalysis(options: String , category: String) {
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
