// GlamArApi.kt

package io.pixelbin.glamar

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import io.pixelbin.glamar.model.Item
import io.pixelbin.glamar.model.SkuItemResponse
import io.pixelbin.glamar.model.SkuListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
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

class GlamArApi(private val accessKey: String, private val development: Boolean = true) {

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            RequestSigningInterceptor(
                signingKey = "1234567",
                headerPrefix = "x-ebg-"
            )
        )
        .build()

    private val gson: Gson
        get() {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(Date::class.java, DateDeserializer())
            return gsonBuilder.create()
        }

    fun fetchSkuList(pageNo: Int, pageSize: Int, callback: (Result<SkuListResponse>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url =
                "${GlamAr.BASE_URL}/service/private/misc/v1.0/skus?pageNo=$pageNo&pageSize=$pageSize"
            val request = Request.Builder()
                .url(url)
                .header(
                    "Authorization",
                    "Bearer ${Base64.getEncoder().encodeToString(accessKey.toByteArray())}"
                )
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

    fun fetchSku(id: String, callback: (Result<Item>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "${GlamAr.BASE_URL}/service/private/misc/v1.0/skus/$id"
            val request = Request.Builder()
                .url(url)
                .header(
                    "Authorization",
                    "Bearer ${Base64.getEncoder().encodeToString(accessKey.toByteArray())}"
                )
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

    private class RequestSigningInterceptor(
        private val signingKey: String,
        private val headerPrefix: String
    ) : Interceptor {

        private val headersToInclude = listOf(Regex("${headerPrefix}.*"), Regex("host"))

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

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

        private fun sortedAndEncodedQueryParams(request: Request): String {
            val allQueryParams = request.url.queryParameterNames.associateWith {
                request.url.queryParameterValues(it).joinToString(",")
            }
            return allQueryParams.toSortedMap().map { (key, value) ->
                "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
            }.joinToString("&")
        }

        private fun generateHmac(secretKey: String, message: String): String {
            val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            val mac = Mac.getInstance("HmacSHA256").apply {
                init(SecretKeySpec(secretKeyBytes, "HmacSHA256"))
            }
            return mac.doFinal(messageBytes).joinToString("") { "%02x".format(it) }
        }

        private fun canonicalHeaders(request: Request): String {
            return request.headers.filter { (name, _) -> headersToInclude.any { it.matches(name) } }
                .sortedBy { it.first }
                .joinToString("\n") { (name, value) -> "$name:${value.trim()}" }
        }

        private fun signedHeaders(request: Request): String {
            return request.headers.names()
                .filter { name -> headersToInclude.any { it.matches(name) } }
                .sortedBy { it.lowercase(Locale.getDefault()) }
                .joinToString(";")
        }

        private fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(input.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
    }
}

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
