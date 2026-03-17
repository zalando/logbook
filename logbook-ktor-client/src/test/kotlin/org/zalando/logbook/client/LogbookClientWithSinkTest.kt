package org.zalando.logbook.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
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
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.zalando.logbook.Correlation
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Logbook
import org.zalando.logbook.Sink
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.test.TestStrategy

@ExperimentalLogbookKtorApi
@OptIn(InternalAPI::class)
internal class LogbookClientWithSinkTest {

    private val port = 8080
    private val server = embeddedServer(CIO, port = port, module = Application::stubApplicationModuleWithSink)

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

    private fun createFixtures(withRetry: Boolean): Pair<Sink, HttpClient> {
        val sink = mock(Sink::class.java)
        `when`(sink.isActive).thenReturn(true)
        `when`(sink.writeBoth(any(), any(), any())).thenCallRealMethod()
        val testLogbook: Logbook = Logbook
            .builder()
            .strategy(TestStrategy())
            .sink(sink)
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
        return sink to client
    }

    @ParameterizedTest(name = "withRetry={0}")
    @MethodSource("withRetry")
    fun `Should log request and response`(withRetry: Boolean) {
        val (sink, client) = createFixtures(withRetry)
        val response = sendAndReceive(client) {
            body = "ping"
        }

        assertThat(response).isNotBlank()

        val capturedRequest = captureRequest(sink)
        assertThat(capturedRequest)
            .isEqualTo("ping")

        val capturedResponse = captureResponse(sink)
        assertThat(capturedResponse)
            .isEqualTo("pong")
    }

    private fun sendAndReceive(client: HttpClient, uri: String = "/ping", block: HttpRequestBuilder.() -> Unit = {}): String {
        return runBlocking {
            client.post(urlString = "http://localhost:$port$uri") {
                block()
            }.body()
        }
    }

    private fun captureRequest(sink: Sink): String {
        return ArgumentCaptor
            .forClass(HttpRequest::class.java)
            .apply { verify(sink, timeout(1_000)).write(any(Correlation::class.java), capture(), any(HttpResponse::class.java)) }
            .value
            .bodyAsString
    }

    private fun captureResponse(sink: Sink): String {
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
