package org.zalando.logbook.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.zalando.logbook.Correlation
import org.zalando.logbook.HttpLogWriter
import org.zalando.logbook.Logbook
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.core.DefaultHttpLogFormatter
import org.zalando.logbook.core.DefaultSink
import org.zalando.logbook.test.TestStrategy

@ExperimentalLogbookKtorApi
@OptIn(InternalAPI::class)
internal class LogbookClientTest {

    private val port = 8080
    private val server = embeddedServer(CIO, port = port, module = Application::stubApplicationModule)

    @BeforeEach
    internal fun setUp() {
        server.start(wait = false)
    }

    @AfterEach
    internal fun tearDown() {
        server.stop(0, 5_000)
    }

    companion object {
        @JvmStatic
        fun withRetry() = listOf(false, true)
    }

    private fun createFixtures(withRetry: Boolean): Pair<HttpLogWriter, HttpClient> {
        val writer = mock(HttpLogWriter::class.java)
        `when`(writer.isActive).thenCallRealMethod()
        val testLogbook: Logbook = Logbook
            .builder()
            .strategy(TestStrategy())
            .sink(DefaultSink(DefaultHttpLogFormatter(), writer))
            .build()
        val client = HttpClient {
            if (withRetry) {
                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = 5)
                    exponentialDelay()
                }
            }
            install(LogbookClient) {
                logbook = testLogbook
            }
        }
        return writer to client
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log request without body`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        sendAndReceive(client)
        val message = captureRequest(writer)
        assertThat(message)
            .startsWith("Outgoing Request:")
            .contains("POST http://localhost:$port/echo HTTP/1.1")
            .doesNotContain("Hello, world!")
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log request with body`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        sendAndReceive(client, "/discard") {
            body = "Hello, world!"
        }
        val message = captureRequest(writer)
        assertThat(message)
            .startsWith("Outgoing Request:")
            .contains("POST http://localhost:$port/discard HTTP/1.1")
            .contains("Hello, world!")
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should not log request if inactive`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        `when`(writer.isActive).thenReturn(false)
        sendAndReceive(client)
        verify(writer, never()).write(any(Precorrelation::class.java), any())
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log response without body`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        sendAndReceive(client, "/discard")
        val message = captureResponse(writer)
        assertThat(message)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK")
            .doesNotContain("Hello, world!")
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log response with body`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        val response = sendAndReceive(client) {
            body = "Hello, world!"
        }
        assertThat(response).isEqualTo("Hello, world!")
        val message = captureResponse(writer)
        assertThat(message)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK")
            .contains("Hello, world!")
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should not log response if inactive`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        `when`(writer.isActive).thenReturn(false)
        sendAndReceive(client) {
            body = "Hello, world!"
        }
        verify(writer, never()).write(any(Correlation::class.java), any())
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log request and response with big body`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        val dataLength = 5_000
        val requestBody = """{"Hello, world!": "${"a".repeat(dataLength)}"}"""
        val response = sendAndReceive(client) {
            body = requestBody
        }

        assertThat(response).isNotBlank()

        val capturedRequest = captureRequest(writer)
        assertThat(capturedRequest)
            .startsWith("Outgoing Request:")
            .contains("POST http://localhost:8080/echo HTTP/1.1")
            .contains("Hello, world!")

        val capturedResponse = captureResponse(writer)
        assertThat(capturedResponse)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK")
            .contains("Hello, world!")
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should ignore bodies`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        val response = sendAndReceive(client) {
            headers["Ignore"] = "true"
            body = "Hello, world!"
        }

        assertThat(response).isEqualTo("Hello, world!")

        run {
            val message = captureRequest(writer)
            assertThat(message)
                .startsWith("Outgoing Request:")
                .contains("POST http://localhost:$port/echo HTTP/1.1")
                .doesNotContain("Hello, world!")
        }

        run {
            val message = captureResponse(writer)
            assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .doesNotContain("Hello, world!")
        }
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `should preserve Content-Type header`(withRetry: Boolean) {
        val (writer, client) = createFixtures(withRetry)
        sendAndReceive(client) {
            body = """{"property": "value"}"""
            headers[org.zalando.logbook.ContentType.CONTENT_TYPE_HEADER] = "application/json"
        }

        val capturedRequest = captureRequest(writer)
        assertThat(capturedRequest)
            .contains("Content-Type: application/json")

        val capturedResponse = captureResponse(writer)
        assertThat(capturedResponse)
            .contains("Content-Type: application/json")
    }

    private fun sendAndReceive(client: HttpClient, uri: String = "/echo", block: HttpRequestBuilder.() -> Unit = {}): String {
        return runBlocking {
            client.post(urlString = "http://localhost:$port$uri") {
                block()
            }.body()
        }
    }

    private fun captureRequest(writer: HttpLogWriter): String {
        return ArgumentCaptor
            .forClass(String::class.java)
            .apply { verify(writer, timeout(1_000)).write(any(Precorrelation::class.java), capture()) }
            .value
    }

    private fun captureResponse(writer: HttpLogWriter): String? {
        return ArgumentCaptor
            .forClass(String::class.java)
            .apply { verify(writer, timeout(1_000)).write(any(Correlation::class.java), capture()) }
            .value
    }
}

fun Application.stubApplicationModule() {
    routing {
        post("/echo") {
            call.respondText(call.receiveText(), call.request.contentType())
        }
        post("/discard") {
            call.receiveText()
            call.respond(HttpStatusCode.OK)
        }
    }
}
