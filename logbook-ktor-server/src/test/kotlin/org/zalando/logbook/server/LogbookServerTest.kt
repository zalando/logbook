package org.zalando.logbook.server

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.zalando.logbook.*
import org.zalando.logbook.common.ExperimentalLogbookKtorApi


@ExperimentalLogbookKtorApi
internal class LogbookServerTest {

    private val port = 8080
    private val writer = mock(HttpLogWriter::class.java)

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(DefaultSink(DefaultHttpLogFormatter(), writer))
        .build()

    private val client = HttpClient {
    }

    private val server = embeddedServer(CIO, port = port) {
        install(LogbookServer) {
            logbook = testLogbook
        }
        routing {
            post("/echo") {
                call.response.headers.append("Content-Type", "Text/Plain", false)
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
            .startsWith("Incoming Request:")
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
            .startsWith("Incoming Request:")
            .contains("POST http://localhost:$port/discard HTTP/1.1")
            .contains("Hello, world!")
    }

    @Test
    fun `Should log request with no content type`() {
        sendAndReceive("/discard") {
            body = "Hello, world!"
            contentType(ContentType.parse(""))
        }
        val message = captureRequest()
        assertThat(message)
            .startsWith("Incoming Request:")
            .contains("POST http://localhost:$port/discard HTTP/1.1")
            .contains("Hello, world!")
            .contains("Content-Type: */*")
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
            .startsWith("Outgoing Response:")
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
            .startsWith("Outgoing Response:")
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
                .startsWith("Incoming Request:")
                .contains("POST http://localhost:$port/echo HTTP/1.1")
                .doesNotContain("Hello, world!")
        }

        run {
            val message = captureResponse()
            assertThat(message)
                .startsWith("Outgoing Response:")
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
