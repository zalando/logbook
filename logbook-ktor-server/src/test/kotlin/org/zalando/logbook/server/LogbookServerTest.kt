package org.zalando.logbook.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.InternalAPI
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
internal class LogbookServerTest {

    private val port = 8080
    private val writer = mock(HttpLogWriter::class.java)

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(
            DefaultSink(
                DefaultHttpLogFormatter(),
                writer
            )
        )
        .build()

    private val client = HttpClient(io.ktor.client.engine.cio.CIO) {
        engine {
            requestTimeout = 0
        }
    }

    private val server = embeddedServer(CIO, port = port) {
        install(LogbookServer) {
            logbook = testLogbook
        }
        routing {
            get("/") {
                call.response.headers.append("Content-Type", "Text/Plain", false)
                call.respondText("Just some text!")
            }
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
    fun `Should log response with body for GET request`() {
        runBlocking {
            client.get(urlString = "http://localhost:$port/") {
            }
        }
        val message = captureResponse()
        assertThat(message)
            .startsWith("Outgoing Response:")
            .contains("HTTP/1.1 200 OK")
            .contains("Content-Type: Text/Plain")
            .contains("Just some text!")
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
            client.post(urlString = "http://localhost:$port$uri") {
                block()
            }.body()
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
