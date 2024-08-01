package io.pixelbin.galmar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Date deserializer for Gson
private class DateDeserializer : JsonDeserializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date? {
        return dateFormat.parse(json.asString)
    }
}

// Main GlamAR class
class GlamAr private constructor(val accessKey: String, val development: Boolean = true) {

    // OkHttpClient with request signing interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(
            RequestSigningInterceptor(
                signingKey = "1234567",
                headerPrefix = "x-ebg-"
            )
        )
        .build()

    // Gson instance with custom date deserializer
    private val gson: Gson
        get() {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(Date::class.java, DateDeserializer())
            return gsonBuilder.create()
        }

    companion object {
        @Volatile
        private var instance: GlamAr? = null

        // Initialize GlamAR singleton
        fun initialize(accessKey: String): GlamAr {
            return instance ?: synchronized(this) {
                instance ?: GlamAr(accessKey).also { instance = it }
            }
        }

        // Get GlamAR instance
        fun getInstance(): GlamAr {
            return instance
                ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }

    // Fetch SKU list from API
    fun fetchSkuList(pageNo: Int, pageSize: Int, callback: (Result<SkuListResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url =
                "https://api.pixelbinz0.de/service/private/misc/v1.0/skus?pageNo=$pageNo&pageSize=$pageSize"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessKey")
                .get()
                .build()
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val skuListResponse: SkuListResponse =
                        gson.fromJson(response.body?.string(), SkuListResponse::class.java)
                    callback(Result.success(skuListResponse))
                } else {
                    callback(Result.failure(IOException("Unexpected code ${response.code} ${response.body?.string()}")))
                }
            } catch (e: IOException) {
                callback(Result.failure(e))
            }
        }
    }

    // Fetch single SKU from API
    fun fetchSku(id: String, callback: (Result<Item>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://api.pixelbinz0.de/service/private/misc/v1.0/skus/$id"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessKey")
                .get()
                .build()
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val skuListResponse: Item =
                        gson.fromJson(response.body?.string(), SkuItemResponse::class.java).item
                    callback(Result.success(skuListResponse))
                } else {
                    callback(Result.failure(IOException("Unexpected code ${response.code} ${response.body?.string()}")))
                }
            } catch (e: IOException) {
                callback(Result.failure(e))
            }
        }
    }


}

// View class for WebView integration
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
        val script = when (previewMode) {
            is PreviewMode.None -> "window.parent.postMessage({ type: 'initialize', payload: { platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true} }, '*');"
            is PreviewMode.Image -> "window.parent.postMessage({ type: 'initialize', payload: { platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openImageOnInit : '${(previewMode as PreviewMode.Image).imageUrl}'} }, '*');"
            is PreviewMode.Camera -> "window.parent.postMessage({ type: 'initialize', payload: { platform: 'android', apiKey:'${GlamAr.getInstance().accessKey}', disableCrossIcon: true, disablePrevIcon: true, openLiveOnInit : true} }, '*');"
        }
        evaluateJavascript(script)
    }

    init {
        addView(webView)
        reloadPage()
        setupWebViewJavaScriptInterface()
    }

    // Initialize the WebView with required parameters
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

    // Update beauty mode based on configuration
    private fun reloadPage(isBeauty: Boolean = false) {
        this.isBeauty = isBeauty
        val host = when {
            GlamAr.getInstance().development && this.isBeauty -> stagingBeautyHost
            GlamAr.getInstance().development && !this.isBeauty -> stagingStyleHost
            !GlamAr.getInstance().development && this.isBeauty -> prodBeautyHost
            else -> prodStyleHost
        }
        webView.loadUrl(host)
    }

    // Apply SKU in the WebView
    fun applySku(skuId: String, category: String) {
        if (category.contains("beauty") != isBeauty) {
            reloadPage(category.contains("beauty"))
        }
        this.skuApplied = skuId
        evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
    }

    // Clear SKU in the WebView
    fun clear() {
        evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
    }

    // Change configuration in the WebView
    fun configChange(options: String, value: String? = null) {
        val script = if (value != null) {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options', value: '$value' }}, '*');"
        } else {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options' }}, '*');"
        }
        evaluateJavascript(script)
    }

    // Take a snapshot in the WebView
    fun snapshot() {
        evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
    }

    // Toggle view in the WebView
    fun toggle(showOriginal: Boolean) {
        val script = if (showOriginal) {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'show'} }, '*');"
        } else {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'hide'} }, '*');"
        }
        evaluateJavascript(script)
    }

    // Evaluate JavaScript in the WebView
    private fun evaluateJavascript(script: String) {
        webView.post {
            Log.d("GlamARView", "Evaluating: $script")
            webView.evaluateJavascript(script) {
                Log.d("GlamARView", "JavaScript evaluation result: $it")
            }
        }
    }

    // Setup JavaScript interface for WebView
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

    // Convert JSONObject to Map
    private fun JSONObject.toMap(): Map<String, Any> {
        return keys().asSequence().associateWith { get(it) }
    }

    // Handle WebView destruction
    fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        defaultCallback = null
    }

    // Handle permission requests
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

    // Handle permission request results
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
private interface Callback {
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


// Interceptor for signing requests
private class RequestSigningInterceptor(
    private val signingKey: String,
    private val headerPrefix: String
) :
    Interceptor {

    private val headersToInclude = listOf(Regex("${headerPrefix}.*"), Regex("host"))

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Getting current formatted time
        val now = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val newRequestBuilder = originalRequest.newBuilder()
            .header("${headerPrefix}param", now)
            .header("host", originalRequest.url.host)

        val canonicalString = generateCanonicalString(newRequestBuilder.build())
        val signature = generateHmac(signingKey, "$now\n${sha256(canonicalString)}")

        newRequestBuilder
            .header("${headerPrefix}signature", "v1:$signature")
            .header("${headerPrefix}param", Base64.getEncoder().encodeToString(now.toByteArray()))

        return chain.proceed(newRequestBuilder.build())
    }

    // Generate canonical string for request
    private fun generateCanonicalString(request: Request): String {
        val content = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        } ?: ""
        val contentHash = sha256(content)
        return "${request.method}\n" +
                "${request.url.encodedPath}\n" +
                "${sortedAndEncodedQueryParams(request)}\n" +
                "${canonicalHeaders(request)}\n\n" +
                "${signedHeaders(request)}\n" +
                contentHash
    }

    // Sort and encode query parameters
    private fun sortedAndEncodedQueryParams(request: Request): String {
        val allQueryParams = request.url.queryParameterNames.associateWith {
            request.url.queryParameterValues(it).joinToString(",")
        }
        return allQueryParams.toSortedMap().map { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }.joinToString("&")
    }

    // Generate HMAC for request signing
    private fun generateHmac(secretKey: String, message: String): String {
        val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val mac = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(secretKeyBytes, "HmacSHA256"))
        }
        return mac.doFinal(messageBytes).joinToString("") { "%02x".format(it) }
    }

    // Generate canonical headers for request
    private fun canonicalHeaders(request: Request): String {
        return request.headers.filter { (name, _) -> headersToInclude.any { it.matches(name) } }
            .sortedBy { it.first }
            .joinToString("\n") { (name, value) -> "$name:${value.trim()}" }
    }

    // Generate signed headers for request
    private fun signedHeaders(request: Request): String {
        return request.headers.names()
            .filter { name -> headersToInclude.any { it.matches(name) } }
            .sortedBy { it.lowercase(Locale.getDefault()) }
            .joinToString(";")
    }

    // Generate SHA-256 hash for input string
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

// Data classes for API responses
data class Page(
    @SerializedName("type") val type: String,
    @SerializedName("size") val size: Int,
    @SerializedName("current") val current: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("itemTotal") val itemTotal: Int
)

data class Attribute(
    @SerializedName("icons") val icons: List<String>,
    @SerializedName("colors") val colors: List<String>,
    @SerializedName("effectAssets") val effectAssets: List<String>
)

data class Meta(
    @SerializedName("material") val material: String,
    @SerializedName("dimension") val dimension: String
)

data class Item(
    @SerializedName("_id") val id: String,
    @SerializedName("orgId") val orgId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("subCategory") val subCategory: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productImage") val productImage: String,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("styleVariant") val styleVariant: String?,
    @SerializedName("styleIcon") val styleIcon: String,
    @SerializedName("attributes") val attributes: List<Attribute>,
    @SerializedName("meta") val meta: Meta,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("updatedAt") val updatedAt: Date
)

data class SkuListResponse(
    @SerializedName("page") val page: Page,
    @SerializedName("items") val items: List<Item>
)

data class SkuItemResponse(
    @SerializedName("sku") val item: Item
)
