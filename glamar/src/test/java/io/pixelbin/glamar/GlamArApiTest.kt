package io.pixelbin.glamar

import io.pixelbin.glamar.model.VersionApiResponse
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger

class GlamArApiTest {
    private val originalApiUrl = GlamAr.API_URL
    private val originalFallbackApiUrl = GlamAr.FALLBACK_API_URL
    private lateinit var primary: VersionServer
    private lateinit var fallback: VersionServer

    @Before
    fun setUp() {
        primary = VersionServer()
        fallback = VersionServer()
        GlamAr.API_URL = primary.url
        GlamAr.FALLBACK_API_URL = fallback.url
    }

    @After
    fun tearDown() {
        primary.close()
        fallback.close()
        GlamAr.API_URL = originalApiUrl
        GlamAr.FALLBACK_API_URL = originalFallbackApiUrl
    }

    @Test
    fun primarySuccessDoesNotRequestFallback() {
        primary.body = """{"success":true,"sdkVersion":"2.1.0"}"""

        assertEquals("2.1.0", getVersion().getOrThrow())
        assertEquals("/service/private/glamar/v3.0/sdk-settings/version", primary.requests.single().path)
        assertNull(primary.requests.single().query)
        assertTrue(fallback.requests.isEmpty())
    }

    @Test
    fun httpFailuresRetryPixelbinWithTheSameAppIdAndAuthentication() {
        fallback.body = """{"success":true,"sdkVersion":"2.0.0"}"""
        val authorization = "Bearer " + Base64.getEncoder().encodeToString("test-key".toByteArray())

        for (status in listOf(401, 403, 404, 429, 500, 503)) {
            primary.status = status
            assertEquals("2.0.0", getVersion("skin app&store=one").getOrThrow())

            val first = primary.requests.last()
            val second = fallback.requests.last()
            assertEquals("/service/private/glamar/v3.0/sdk-settings/version", first.path)
            assertEquals("/service/private/misc/v3.0/sdk-settings/version", second.path)
            assertEquals("appId=skin%20app%26store%3Done", first.query)
            assertEquals(first.query, second.query)
            assertEquals(authorization, first.authorization)
            assertEquals(authorization, second.authorization)
            assertTrue(first.signature?.startsWith("v1:") == true)
            assertTrue(second.signature?.startsWith("v1:") == true)
        }
        assertEquals(6, primary.requests.size)
        assertEquals(6, fallback.requests.size)
    }

    @Test
    fun connectionFailureRetriesPixelbin() {
        primary.close()
        fallback.body = """{"sdkVersion":"2.0.0"}"""

        assertEquals("2.0.0", getVersion().getOrThrow())
        assertEquals(1, fallback.requests.size)
    }

    @Test
    fun bothApisFailReturnsFailure() {
        primary.status = 500
        fallback.status = 503

        val result = getVersion()

        assertTrue(result.isFailure)
        assertEquals("HTTP 503", result.exceptionOrNull()?.message)
        assertEquals(1, primary.requests.size)
        assertEquals(1, fallback.requests.size)
    }

    @Test
    fun successfulResponseWithoutVersionKeepsLocalVersionFallback() {
        for (body in listOf("{}", "null", "")) {
            primary.body = body
            val result = getVersion(" ")
            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
            assertNull(primary.requests.last().query)
        }
        primary.body = """{"sdkVersion":""}"""
        assertEquals("", getVersion().getOrThrow())
        assertTrue(fallback.requests.isEmpty())
    }

    @Test
    fun invalidPrimaryResponseRetriesPixelbin() {
        primary.body = "{invalid json"
        fallback.body = """{"sdkVersion":"2.0.0"}"""

        assertEquals("2.0.0", getVersion().getOrThrow())
        assertEquals(1, fallback.requests.size)
    }

    @Test
    fun invalidFallbackResponseReturnsFailure() {
        primary.status = 500
        fallback.body = "{invalid json"

        assertTrue(getVersion().isFailure)
        assertEquals(1, fallback.requests.size)
    }

    @Test
    fun diagnosticsIncludeBothHttpResponsesInOrder() {
        primary.status = 401
        primary.body = """{"error":"invalid access key"}"""
        fallback.body = """{"sdkVersion":"2.0.0"}"""
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        assertEquals("2.0.0", getVersion("skin-app") { responses.add(it) }.getOrThrow())

        assertEquals(2, responses.size)
        assertEquals("${primary.url}/service/private/glamar/v3.0/sdk-settings/version?appId=skin-app", responses[0].url)
        assertEquals(401, responses[0].statusCode)
        assertEquals(primary.body, responses[0].body)
        assertEquals("HTTP 401", responses[0].error)
        assertEquals("${fallback.url}/service/private/misc/v3.0/sdk-settings/version?appId=skin-app", responses[1].url)
        assertEquals(200, responses[1].statusCode)
        assertEquals(fallback.body, responses[1].body)
        assertNull(responses[1].error)
    }

    @Test
    fun diagnosticsIncludeConnectionErrorsWithoutInventingAnHttpResponse() {
        primary.close()
        fallback.body = """{"sdkVersion":"2.0.0"}"""
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        assertEquals("2.0.0", getVersion(onResponse = { responses.add(it) }).getOrThrow())

        assertEquals(2, responses.size)
        assertNull(responses[0].statusCode)
        assertNull(responses[0].body)
        assertFalse(responses[0].error.isNullOrBlank())
        assertEquals(200, responses[1].statusCode)
    }

    @Test
    fun diagnosticsPreserveMalformedResponseBodyAndParsingError() {
        primary.body = "{invalid json"
        fallback.body = """{"sdkVersion":"2.0.0"}"""
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        assertEquals("2.0.0", getVersion(onResponse = { responses.add(it) }).getOrThrow())

        assertEquals(2, responses.size)
        assertEquals(200, responses[0].statusCode)
        assertEquals(primary.body, responses[0].body)
        assertFalse(responses[0].error.isNullOrBlank())
    }

    @Test
    fun diagnosticListenerFailureDoesNotInterruptFallbackOrCompletion() {
        primary.status = 500
        fallback.body = """{"sdkVersion":"2.0.0"}"""
        val attempts = AtomicInteger()

        val result = getVersion(onResponse = {
            attempts.incrementAndGet()
            throw IllegalStateException("Diagnostic listener failed")
        })

        assertEquals("2.0.0", result.getOrThrow())
        assertEquals(2, attempts.get())
    }

    private fun getVersion(
        appId: String? = null,
        onResponse: ((VersionApiResponse) -> Unit)? = null
    ): Result<String?> {
        val completed = CountDownLatch(1)
        val result = AtomicReference<Result<String?>>()
        val callback: (Result<String?>) -> Unit = {
            result.set(it)
            completed.countDown()
        }
        val api = GlamArApi("test-key")
        if (onResponse == null) {
            api.getVersion(appId, callback)
        } else {
            api.getVersion(appId = appId, onResponse = onResponse, callback = callback)
        }
        assertTrue("Version callback was not invoked", completed.await(5, TimeUnit.SECONDS))
        return result.get()
    }

    private data class RecordedRequest(
        val path: String,
        val query: String?,
        val authorization: String?,
        val signature: String?
    )

    private class VersionServer {
        var status = 200
        var body = "{}"
        val requests = CopyOnWriteArrayList<RecordedRequest>()
        private val server = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse {
                    requests.add(RecordedRequest(
                        request.requestUrl!!.encodedPath,
                        request.requestUrl!!.encodedQuery,
                        request.getHeader("Authorization"),
                        request.getHeader("x-ebg-signature")
                    ))
                    return MockResponse().setResponseCode(status).setBody(body)
                }
            }
            start()
        }
        val url = server.url("/").toString().removeSuffix("/")

        fun close() = server.shutdown()
    }
}
