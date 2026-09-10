// GlamAr.kt
package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import io.pixelbin.glamar.model.ApplyCatalogOptions
import io.pixelbin.glamar.model.ConfigData
import io.pixelbin.glamar.model.GlamAROverrides
import org.json.JSONArray
import org.json.JSONObject

class GlamAr private constructor(val accessKey: String) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GlamAr? = null
        private const val DEBUG_BASE_URL = "https://cdn.glamar.io/sdk"
        private const val DEBUG_API_URL = "https://api.glamar.fynd.com"
        private const val DEBUG_FALLBACK_API_URL = "https://api.pixelbin.io"
        private const val PRODUCTION_BASE_URL = "https://cdn.glamar.io/sdk"
        private const val PRODUCTION_API_URL = "https://api.glamar.fynd.com"
        private const val PRODUCTION_FALLBACK_API_URL = "https://api.pixelbin.io"

        var BASE_URL = PRODUCTION_BASE_URL
        var API_URL = PRODUCTION_API_URL
        var FALLBACK_API_URL = PRODUCTION_FALLBACK_API_URL


        @SuppressLint("SetJavaScriptEnabled")
        fun init(
            context: Context,
            accessKey: String,
            overrides: GlamAROverrides? = null,
            webView: WebView? = null,
            debug: Boolean = false,
        ): GlamAr {
            GlamArLogger.init(debug)
            configureUrls(debug)

            // Reinitialization must use the credentials supplied for the new session.
            val inst = synchronized(this) {
                instance?.takeIf { it.accessKey == accessKey }
                    ?: GlamAr(accessKey).also { instance = it }
            }

            // 2) Now it’s safe to prepare the WebView (which eventually calls getInstance())
            GlamArWebViewManager.prepareWebView(
                context = context,
                overrides = overrides,
                providedWebView = webView
            )

            return inst
        }

        private fun configureUrls(debug: Boolean) {
            BASE_URL = if (debug) DEBUG_BASE_URL else PRODUCTION_BASE_URL
            val (apiUrl, fallbackApiUrl) = versionApiUrls(debug)
            API_URL = apiUrl
            FALLBACK_API_URL = fallbackApiUrl
        }

        internal fun versionApiUrls(debug: Boolean?): Pair<String, String> {
            return when (debug) {
                true -> DEBUG_API_URL to DEBUG_FALLBACK_API_URL
                false -> PRODUCTION_API_URL to PRODUCTION_FALLBACK_API_URL
                null -> API_URL to FALLBACK_API_URL
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

        fun applyBySku(skuId: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
        }
        @JvmOverloads
        fun applyByCategory(category: String, options: ApplyCatalogOptions? = null) {
            val payload = if (options != null) {
                JSONObject()
                    .put("category", category)
                    .put(
                        "options",
                        JSONObject().apply {
                            options.storeFront?.let { put("storeFront", it) }
                        }
                    )
                    .toString()
            } else {
                JSONObject.quote(category)
            }

            evaluateJavascript("window.parent.postMessage({ type: 'applyByCategory', payload: $payload }, '*');")
        }

        @JvmOverloads
        fun applyBySubCategory(subCategory: String, options: ApplyCatalogOptions? = null) {
            val payload = if (options != null) {
                JSONObject()
                    .put("subCategory", subCategory)
                    .put(
                        "options",
                        JSONObject().apply {
                            options.storeFront?.let { put("storeFront", it) }
                        }
                    )
                    .toString()
            } else {
                JSONObject.quote(subCategory)
            }

            evaluateJavascript("window.parent.postMessage({ type: 'applyBySubCategory', payload: $payload }, '*');")
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

        @JvmOverloads
        fun configChange(
            type: String,
            value: Number? = null,
            skuId: String? = null,
            subCategory: String? = null
        ) {
            val configData = ConfigData(
                type = type,
                value = value,
                skuId = skuId,
                subCategory = subCategory
            )
            val payload = JSONObject().put("type", configData.type)

            configData.value?.let { payload.put("value", it) }
            configData.skuId?.let { payload.put("skuId", it) }
            configData.subCategory?.let { payload.put("subCategory", it) }

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

        @JvmOverloads
        fun reset(value: Any? = null) {
            sendClearSku(normalizeClearSkuPayload(value))
        }

        fun skinAnalysis(options: String) {
            evaluateJavascript("window.parent.postMessage({ type: 'skinAnalysis' , payload: { options: '${options}' }  }, '*');")
        }

        fun setViewportMirrored(state: Boolean) {
          val option = if (state == true) "start" else "close"
          val payload = JSONObject()
            .put("options", option)

          evaluateJavascript("window.parent.postMessage({ type: 'mirrorMode', payload: $payload }, '*');")
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

        private fun emitError(message: String) {
            val payload = JSONObject()
                .put("message", message)

            GlamArLogger.e("GlamAR", message)
            GlamArEventManager.dispatchEvent("error", payload)
        }

        private fun normalizeClearSkuPayload(value: Any?): JSONObject? {
            if (value == null) return null

            if (value is String) {
                return if (value.isEmpty()) {
                    null
                } else {
                    JSONObject().put("subCategory", value)
                }
            }

            if (value !is Map<*, *>) return null

            val payload = JSONObject()
            val subCategory = value["subCategory"]
            val skuIds = normalizeSkuIds(value["skuIds"])

            if (subCategory is String && subCategory.isNotEmpty()) {
                payload.put("subCategory", subCategory)
            }

            if (skuIds != null && skuIds.length() > 0) {
                payload.put("skuIds", skuIds)
            }

            return if (payload.length() == 0) null else payload
        }

        private fun normalizeSkuIds(value: Any?): JSONArray? {
            val items = when (value) {
                is Collection<*> -> value.toList()
                is Array<*> -> value.toList()
                is JSONArray -> (0 until value.length()).map { value.opt(it) }
                else -> return null
            }

            if (items.isEmpty() || items.any { it !is String }) return null

            return JSONArray().apply {
                items.forEach { put(it) }
            }
        }

        private fun sendClearSku(payload: JSONObject?) {
            if (payload != null) {
                evaluateJavascript("window.parent.postMessage({ type: 'clearSku', payload: $payload }, '*');")
            } else {
                evaluateJavascript("window.parent.postMessage({ type: 'clearSku' }, '*');")
            }
        }

        fun getInstance(): GlamAr {
            return instance ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
