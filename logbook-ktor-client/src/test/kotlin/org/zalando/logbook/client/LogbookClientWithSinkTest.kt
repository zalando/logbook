package org.zalando.logbook.client

import io.ktor.server.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.InternalAPI
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
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Logbook
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.Sink
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.core.DefaultHttpLogFormatter
import org.zalando.logbook.core.DefaultSink
import org.zalando.logbook.test.TestStrategy
import kotlin.math.sin

@ExperimentalLogbookKtorApi
@OptIn(InternalAPI::class)
internal class LogbookClientWithSinkTest {

    private val port = 8080
    private val sink = mock(Sink::class.java)

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(sink)
        .build()

    private val client = HttpClient {
        install(LogbookClient) {
            logbook = testLogbook
        }
    }

    private val server = embeddedServer(CIO, port = port, module = Application::stubApplicationModuleWithSink)

    @BeforeEach
    internal fun setUp() {
        server.start(wait = false)
        `when`(sink.isActive).thenReturn(true)
        `when`(sink.writeBoth(any(), any(), any())).thenCallRealMethod()
    }

    @AfterEach
    internal fun tearDown() {
        server.stop(0, 5_000)
    }

    @Test
    fun `Should log request and response`() {
        val response = sendAndReceive() {
            body = "ping"
        }

        assertThat(response).isNotBlank()

        val capturedRequest = captureRequest()
        assertThat(capturedRequest)
            .isEqualTo("ping")

        val capturedResponse = captureResponse()
        assertThat(capturedResponse)
            .isEqualTo("pong")
    }


    private fun sendAndReceive(uri: String = "/ping", block: HttpRequestBuilder.() -> Unit = {}): String {
        return runBlocking {
            client.post(urlString = "http://localhost:$port$uri") {
                block()
            }.body()
        }
    }

    private fun captureRequest(): String {
        return ArgumentCaptor
            .forClass(HttpRequest::class.java)
            .apply { verify(sink, timeout(1_000)).write(any(Correlation::class.java), capture(), any(HttpResponse::class.java)) }
            .value
            .bodyAsString
    }

    private fun captureResponse(): String? {
        return ArgumentCaptor
            .forClass(HttpResponse::class.java)
            .apply { verify(sink, timeout(1_000)).write(any(Correlation::class.java), any(HttpRequest::class.java), capture()) }
            .value
            .bodyAsString
    }
}

fun Application.stubApplicationModuleWithSink() {
    routing {
        post("/ping") {
            call.respondText("pong", ContentType.Text.Plain)
        }
    }
}
