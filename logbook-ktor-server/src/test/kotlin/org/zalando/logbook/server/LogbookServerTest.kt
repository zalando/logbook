package org.zalando.logbook.server

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import org.mockito.ArgumentMatchers.any
import org.zalando.logbook.*
import org.zalando.logbook.common.ExperimentalLogbookKtorApi


@ExperimentalLogbookKtorApi
internal class LogbookServerTest : FunSpec() {

    private val writer: HttpLogWriter = mockk()

    private val testLogbook: Logbook = Logbook
        .builder()
        .strategy(TestStrategy())
        .sink(DefaultSink(DefaultHttpLogFormatter(), writer))
        .build()

    private val requestMessageSlot = slot<String>()
    private val responseMessageSlot = slot<String>()

    init {
        beforeAny {
            every { writer.isActive } answers { callOriginal() }
            justRun { writer.write(any<Precorrelation>(), capture(requestMessageSlot)) }
            justRun { writer.write(any<Correlation>(), capture(responseMessageSlot)) }
        }

        test("should log request without body") {
            testApplication {
                server()
                createClient {}.post("/echo")
                val requestMessage = requestMessageSlot.captured
                assertSoftly {
                    requestMessage shouldStartWith "Incoming Request:"
                    requestMessage shouldContain "POST http://localhost/echo HTTP/1.1"
                    requestMessage shouldNotContain "Hello, world!"
                }
            }
        }

        test("should log request with body") {
            testApplication {
                server()
                createClient {}.post("/discard") {
                    setBody("Hello, world!")
                }
                val requestMessage = requestMessageSlot.captured
                assertSoftly {
                    requestMessage shouldStartWith "Incoming Request:"
                    requestMessage shouldContain "POST http://localhost/discard HTTP/1.1"
                    requestMessage shouldContain "Hello, world!"
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
                val requestMessage = requestMessageSlot.captured
                assertSoftly {
                    requestMessage shouldStartWith "Incoming Request:"
                    requestMessage shouldContain "POST http://localhost/discard HTTP/1.1"
                    requestMessage shouldContain "Hello, world!"
                    requestMessage shouldContain "Content-Type: */*"
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
        test("should log responseMessage without body") {
            justRun { writer.write(any<Correlation>(), capture(responseMessageSlot)) }
            testApplication {
                server()
                createClient {}.post("/discard")
                val responseMessage = responseMessageSlot.captured
                assertSoftly {
                    responseMessage shouldStartWith "Outgoing Response:"
                    responseMessage shouldContain "HTTP/1.1 200 OK"
                    responseMessage shouldNotContain "Hello, world!"
                }
            }
        }
        test("should log responseMessage with body") {
            justRun { writer.write(any<Correlation>(), capture(responseMessageSlot)) }
            testApplication {
                server()
                val httpResponse = createClient {}.post("/echo") {
                    setBody("Hello, world!")
                }
                httpResponse.body<String>() shouldBe "Hello, world!"
                val responseMessage = responseMessageSlot.captured
                assertSoftly {
                    responseMessage shouldStartWith "Outgoing Response:"
                    responseMessage shouldContain "HTTP/1.1 200 OK"
                    responseMessage shouldContain "Hello, world!"
                }
            }
        }
        test("should not log responseMessage if inactive") {
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
            justRun { writer.write(any<Correlation>(), capture(responseMessageSlot)) }
            testApplication {
                server()
                val httpResponse = createClient { }.post("/echo") {
                    headers["Ignore"] = "true"
                    setBody("Hello, world!")
                }
                httpResponse.body<String>() shouldBe "Hello, world!"

                val requestMessage = requestMessageSlot.captured
                assertSoftly {
                    requestMessage shouldStartWith "Incoming Request:"
                    requestMessage shouldContain "POST http://localhost/echo HTTP/1.1"
                    requestMessage shouldNotContain "Hello, world!"
                }

                val responseMessage = responseMessageSlot.captured
                assertSoftly {
                    responseMessage shouldStartWith "Outgoing Response:"
                    responseMessage shouldContain "HTTP/1.1 200 OK"
                    responseMessage shouldNotContain "Hello, world!"
                }
            }
        }
        /*
            There is an issue if a route uses receiveText() instead of receive<String>()
            https://youtrack.jetbrains.com/issue/KTOR-5802/
         */
        test("test transformBody not being executed") {
            testApplication {
                server()
                createClient { }.post("/receive-text")
                verify(exactly = 0) {
                    writer.write(any(Precorrelation::class.java), any())
                }
                verify(exactly = 0) {
                    writer.write(any(Correlation::class.java), any())
                }
            }
        }
    }

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
            post("/receive-text") {
                call.receiveText()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
