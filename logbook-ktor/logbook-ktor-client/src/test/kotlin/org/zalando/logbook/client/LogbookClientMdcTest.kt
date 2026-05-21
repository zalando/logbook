package org.zalando.logbook.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.zalando.logbook.Correlation
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Logbook
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.Sink
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests that MDC context is preserved when logging HTTP responses.
 *
 * The LogbookClient uses scope.launch to log responses asynchronously.
 * If coroutineContext is not passed to scope.launch, MDC values will be lost
 * because MDC is thread-local and doesn't automatically propagate to new coroutines.
 */
@ExperimentalLogbookKtorApi
@OptIn(InternalAPI::class)
internal class LogbookClientMdcTest {

    private val port = 8082
    private val server = embeddedServer(CIO, port = port, module = Application::mdcTestModule)

    @BeforeEach
    internal fun setUp() {
        server.start(wait = false)
    }

    @AfterEach
    internal fun tearDown() {
        server.stop(0, 5_000)
        MDC.clear()
    }

    @Test
    fun `Should preserve MDC context in response logging`() {
        val capturedMdcValues = CopyOnWriteArrayList<String?>()
        val responseLatch = CountDownLatch(1)
        
        val sink = object : Sink {
            override fun write(precorrelation: Precorrelation, request: HttpRequest) {
                // Request logging - MDC should be available here
                capturedMdcValues.add("request:" + MDC.get("traceId"))
            }

            override fun write(correlation: Correlation, request: HttpRequest, response: HttpResponse) {
                // Response logging - MDC should also be available here if coroutineContext is propagated
                capturedMdcValues.add("response:" + MDC.get("traceId"))
                responseLatch.countDown()
            }
        }

        val testLogbook = Logbook.builder()
            .sink(sink)
            .build()

        val client = HttpClient {
            install(LogbookClient) {
                logbook = testLogbook
            }
        }

        val traceId = "test-trace-12345"
        MDC.put("traceId", traceId)
        
        try {
            runBlocking(MDCContext()) {
                val response: String = client.post("http://localhost:$port/ping").body()
                assertThat(response).isEqualTo("pong")
            }
        } finally {
            MDC.clear()
        }

        // Wait for async response logging to complete
        val completed = responseLatch.await(5, TimeUnit.SECONDS)
        assertThat(completed)
            .withFailMessage("Response logging did not complete within timeout")
            .isTrue()

        // Verify MDC was captured in both request and response logging
        assertThat(capturedMdcValues)
            .withFailMessage("Expected 2 captured MDC values (request + response), got: $capturedMdcValues")
            .hasSize(2)
        
        assertThat(capturedMdcValues[0])
            .withFailMessage("Request logging should have MDC traceId")
            .isEqualTo("request:$traceId")
        
        assertThat(capturedMdcValues[1])
            .withFailMessage("Response logging should have MDC traceId, but it was lost. " +
                "This indicates coroutineContext is not being passed to scope.launch() in LogbookClient")
            .isEqualTo("response:$traceId")

        client.close()
    }
}

fun Application.mdcTestModule() {
    routing {
        post("/ping") {
            call.respondText("pong")
        }
    }
}
