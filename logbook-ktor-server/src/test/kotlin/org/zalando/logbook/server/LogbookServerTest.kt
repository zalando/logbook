package org.zalando.logbook.server

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.mockito.ArgumentMatchers.any
import org.zalando.logbook.Correlation
import org.zalando.logbook.DefaultHttpLogFormatter
import org.zalando.logbook.DefaultSink
import org.zalando.logbook.HttpLogWriter
import org.zalando.logbook.Logbook
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.TestStrategy
import org.zalando.logbook.common.ExperimentalLogbookKtorApi


@ExperimentalLogbookKtorApi
internal class LogbookServerTest : FunSpec() {

    private val writer: HttpLogWriter = mockk()

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(DefaultSink(DefaultHttpLogFormatter(), writer))
        .build()

    private val messageSlot = slot<String>()

    private fun ApplicationTestBuilder.server() {
        application {
            install(LogbookServer) {
                logbook = testLogbook
            }
        }

        routing {
            post("/echo") {
                val text = call.receive<String>()
                call.response.headers.append("Content-Type", "Text/Plain", false)
                call.respondText(text)
            }
            post("/discard") {
                call.receive<String>()
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    init {
        beforeAny {
            every { writer.isActive } answers {
                callOriginal()
            }
            justRun { writer.write(any<Precorrelation>(), capture(messageSlot)) }
            justRun { writer.write(any<Correlation>(), any()) }
        }

        test("should log request without body") {
            testApplication {
                server()
                createClient {}.post("/echo")
                val message = messageSlot.captured
                assertSoftly {
                    message shouldStartWith "Incoming Request:"
                    message shouldContain "POST http://localhost/echo HTTP/1.1"
                    message shouldNotContain "Hello, world!"
                }
            }
        }

        test("should log request with body") {
            testApplication {
                server()
                createClient {}.post("/discard") {
                    setBody("Hello, world!")
                }
                val message = messageSlot.captured
                assertSoftly {
                    message shouldStartWith "Incoming Request:"
                    message shouldContain "POST http://localhost/discard HTTP/1.1"
                    message shouldContain "Hello, world!"
                }
            }
        }
        test("should log request with no content type") {
            testApplication {
                server()
                createClient {}.post("/discard") {
                    setBody("Hello, world!")
                    contentType(ContentType.parse(""))
                }
                val message = messageSlot.captured
                assertSoftly {
                    message shouldStartWith "Incoming Request:"
                    message shouldContain "POST http://localhost/discard HTTP/1.1"
                    message shouldContain "Hello, world!"
                    message shouldContain "Content-Type: */*"
                }
            }
        }
        test("should not log request if inactive") {
            every { writer.isActive } returns false
            testApplication {
                server()
                createClient {}.post("/echo")
                io.mockk.verify(exactly = 0) {
                    writer.write(any(Precorrelation::class.java), any())
                }
            }
        }
        test("should log response without body") {
            val responseSlot = slot<String>()
            justRun { writer.write(any<Correlation>(), capture(responseSlot)) }
            testApplication {
                server()
                createClient {}.post("/discard")
                val response = responseSlot.captured
                assertSoftly {
                    response shouldStartWith "Outgoing Response:"
                    response shouldContain "HTTP/1.1 200 OK"
                    response shouldNotContain "Hello, world!"
                }
            }
        }
        test("should log response with body") {
            val responseSlot = slot<String>()
            justRun { writer.write(any<Correlation>(), capture(responseSlot)) }
            testApplication {
                server()
                val httpResponse = createClient {}.post("/echo") {
                    setBody("Hello, world!")
                }
                httpResponse.body<String>() shouldBe "Hello, world!"
                val response = responseSlot.captured
                assertSoftly {
                    response shouldStartWith "Outgoing Response:"
                    response shouldContain "HTTP/1.1 200 OK"
                    response shouldContain "Hello, world!"
                }
            }
        }
        test("should not log response if inactive") {
            every { writer.isActive } returns false
            testApplication {
                server()
                createClient {}.post("/echo")
                io.mockk.verify(exactly = 0) {
                    writer.write(any(Correlation::class.java), any())
                }
            }
        }
        test("should ignore bodies") {
            val responseSlot = slot<String>()
            justRun { writer.write(any<Correlation>(), capture(responseSlot)) }
            testApplication {
                server()
                val httpResponse = createClient { }.post("/echo") {
                    headers["Ignore"] = "true"
                    setBody("Hello, world!")
                }
                httpResponse.body<String>() shouldBe "Hello, world!"

                val message = messageSlot.captured
                assertSoftly {
                    message shouldStartWith "Incoming Request:"
                    message shouldContain "POST http://localhost/echo HTTP/1.1"
                    message shouldNotContain "Hello, world!"
                }

                val response = responseSlot.captured
                assertSoftly {
                    response shouldStartWith "Outgoing Response:"
                    response shouldContain "HTTP/1.1 200 OK"
                    response shouldNotContain "Hello, world!"
                }
            }
        }
    }
}
