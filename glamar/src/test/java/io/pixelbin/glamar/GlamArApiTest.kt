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
    private lateinit var primary: VersionServer

    @Before
    fun setUp() {
        primary = VersionServer()
        GlamAr.API_URL = primary.url
    }

    @After
    fun tearDown() {
        primary.close()
        GlamAr.API_URL = originalApiUrl
    }

    @Test
    fun successfulVersionLookupUsesGlamarRoute() {
        primary.body = """{"success":true,"sdkVersion":"2.1.0"}"""

        assertEquals("2.1.0", getVersion().getOrThrow())
        assertEquals("/service/private/glamar/v3.0/sdk-settings/version", primary.requests.single().path)
        assertNull(primary.requests.single().query)
        assertEquals(1, primary.requests.size)
    }

    @Test
    fun httpFailuresReturnDirectlyWithAppIdAndAuthenticationPreserved() {
        val authorization = "Bearer " + Base64.getEncoder().encodeToString("test-key".toByteArray())

        for (status in listOf(401, 403, 404, 429, 500, 503)) {
            primary.status = status
            val result = getVersion("skin app&store=one")
            assertTrue(result.isFailure)
            assertEquals("HTTP $status", result.exceptionOrNull()?.message)

            val first = primary.requests.last()
            assertEquals("/service/private/glamar/v3.0/sdk-settings/version", first.path)
            assertEquals("appId=skin%20app%26store%3Done", first.query)
            assertEquals(authorization, first.authorization)
            assertTrue(first.signature?.startsWith("v1:") == true)
        }
        assertEquals(6, primary.requests.size)
    }

    @Test
    fun connectionFailureReturnsFailure() {
        primary.close()

        assertTrue(getVersion().exceptionOrNull() is java.io.IOException)
    }

    @Test
    fun debugAndProductionUseFyndApi() {
        assertEquals("https://api.glamar.fynd.com", GlamAr.versionApiUrl(true))
        assertEquals("https://api.glamar.fynd.com", GlamAr.versionApiUrl(false))
        assertEquals(primary.url, GlamAr.versionApiUrl(null))
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
        assertEquals(4, primary.requests.size)
    }

    @Test
    fun invalidResponseReturnsFailure() {
        primary.body = "{invalid json"

        assertTrue(getVersion().isFailure)
        assertEquals(1, primary.requests.size)
    }

    @Test
    fun diagnosticsIncludeOnlyTheFyndFailureResponse() {
        primary.status = 401
        primary.body = """{"error":"invalid access key"}"""
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        val result = getVersion("skin-app") { responses.add(it) }
        assertTrue(result.isFailure)
        assertEquals("HTTP 401", result.exceptionOrNull()?.message)

        assertEquals(1, responses.size)
        assertEquals("${primary.url}/service/private/glamar/v3.0/sdk-settings/version?appId=skin-app", responses[0].url)
        assertEquals(401, responses[0].statusCode)
        assertEquals(primary.body, responses[0].body)
        assertEquals("HTTP 401", responses[0].error)
        assertEquals(1, primary.requests.size)
    }

    @Test
    fun diagnosticsIncludeConnectionErrorsWithoutInventingAnHttpResponse() {
        primary.close()
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        assertTrue(getVersion(onResponse = { responses.add(it) }).isFailure)

        assertEquals(1, responses.size)
        assertNull(responses[0].statusCode)
        assertNull(responses[0].body)
        assertFalse(responses[0].error.isNullOrBlank())
    }

    @Test
    fun diagnosticsPreserveMalformedResponseBodyAndParsingError() {
        primary.body = "{invalid json"
        val responses = CopyOnWriteArrayList<VersionApiResponse>()

        assertTrue(getVersion(onResponse = { responses.add(it) }).isFailure)

        assertEquals(1, responses.size)
        assertEquals(200, responses[0].statusCode)
        assertEquals(primary.body, responses[0].body)
        assertFalse(responses[0].error.isNullOrBlank())
    }

    @Test
    fun diagnosticListenerFailureDoesNotInterruptCompletion() {
        primary.body = """{"sdkVersion":"2.0.0"}"""
        val attempts = AtomicInteger()

        val result = getVersion(onResponse = {
            attempts.incrementAndGet()
            throw IllegalStateException("Diagnostic listener failed")
        })

        assertEquals("2.0.0", result.getOrThrow())
        assertEquals(1, attempts.get())
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
