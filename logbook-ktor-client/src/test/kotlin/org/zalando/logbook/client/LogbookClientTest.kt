package org.zalando.logbook.client

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.zalando.logbook.api.Correlation
import org.zalando.logbook.api.HttpLogWriter
import org.zalando.logbook.api.Logbook
import org.zalando.logbook.api.Precorrelation
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.core.DefaultHttpLogFormatter
import org.zalando.logbook.core.DefaultSink
import org.zalando.logbook.test.TestStrategy


@ExperimentalLogbookKtorApi
internal class LogbookClientTest {

    private val port = 8080
    private val writer = mock(HttpLogWriter::class.java)

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(DefaultSink(DefaultHttpLogFormatter(), writer))
        .build()

    private val client = HttpClient {
        install(LogbookClient) {
            logbook = testLogbook
        }
    }

    private val server = embeddedServer(CIO, port = port) {
        routing {
            post("/echo") {
                call.respondText(call.receiveText())
            }
            post("/discard") {
                call.receiveText()
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    @BeforeEach
    internal fun setUp() {
        server.start(wait = false)
        `when`(writer.isActive).thenCallRealMethod()
    }

    @AfterEach
    internal fun tearDown() {
        server.stop(0, 5_000)
    }

    @Test
    fun `Should log request without body`() {
        sendAndReceive()
        val message = captureRequest()
        assertThat(message)
            .startsWith("Outgoing Request:")
            .contains("POST http://localhost:$port/echo HTTP/1.1")
            .doesNotContain("Hello, world!")
    }

    @Test
    fun `Should log request with body`() {
        sendAndReceive("/discard") {
            body = "Hello, world!"
        }
        val message = captureRequest()
        assertThat(message)
            .startsWith("Outgoing Request:")
            .contains("POST http://localhost:$port/discard HTTP/1.1")
            .contains("Hello, world!")
    }

    @Test
    fun `Should not log request if inactive`() {
        `when`(writer.isActive).thenReturn(false)
        sendAndReceive()
        verify(writer, never()).write(any(Precorrelation::class.java), any())
    }

    @Test
    fun `Should log response without body`() {
        sendAndReceive("/discard")
        val message = captureResponse()
        assertThat(message)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK")
            .doesNotContain("Hello, world!")
    }

    @Test
    fun `Should log response with body`() {
        val response = sendAndReceive {
            body = "Hello, world!"
        }
        assertThat(response).isEqualTo("Hello, world!")
        val message = captureResponse()
        assertThat(message)
            .startsWith("Incoming Response:")
            .contains("HTTP/1.1 200 OK")
            .contains("Hello, world!")
    }

    @Test
    fun `Should not log response if inactive`() {
        `when`(writer.isActive).thenReturn(false)
        sendAndReceive()
        verify(writer, never()).write(any(Correlation::class.java), any())
    }

    @Test
    fun `Should ignore bodies`() {
        val response = sendAndReceive {
            headers["Ignore"] = "true"
            body = "Hello, world!"
        }

        assertThat(response).isEqualTo("Hello, world!")

        run {
            val message = captureRequest()
            assertThat(message)
                .startsWith("Outgoing Request:")
                .contains("POST http://localhost:$port/echo HTTP/1.1")
                .doesNotContain("Hello, world!")
        }

        run {
            val message = captureResponse()
            assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .doesNotContain("Hello, world!")
        }
    }

    private fun sendAndReceive(uri: String = "/echo", block: HttpRequestBuilder.() -> Unit = {}): String {
        return runBlocking {
            client.post(urlString = "http://localhost:$port${uri}") {
                block()
            }
        }
    }

    private fun captureRequest(): String {
        return ArgumentCaptor
            .forClass(String::class.java)
            .apply { verify(writer, timeout(1_000)).write(any(Precorrelation::class.java), capture()) }
            .value
    }

    private fun captureResponse(): String? {
        return ArgumentCaptor
            .forClass(String::class.java)
            .apply { verify(writer, timeout(1_000)).write(any(Correlation::class.java), capture()) }
            .value
    }
}
