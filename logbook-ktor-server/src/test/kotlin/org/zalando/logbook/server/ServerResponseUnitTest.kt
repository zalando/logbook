package org.zalando.logbook.server

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.server.response.*
import io.mockk.every
import io.mockk.mockk
import kotlin.text.Charsets.US_ASCII


internal class ServerResponseUnitTest : FunSpec() {

    private val applicationResponse = mockk<ApplicationResponse>()

    init {
        test("ServerResponse unit test") {
            every { applicationResponse.status() } returns HttpStatusCode.Accepted
            every { applicationResponse.headers } returns object : ResponseHeaders() {
                override fun engineAppendHeader(name: String, value: String) = Unit
                override fun getEngineHeaderNames(): List<String> = listOf(HttpHeaders.ContentType)
                override fun getEngineHeaderValues(name: String): List<String> =
                    listOf("application/json; charset=us-ascii")
            }
            val response = ServerResponse(applicationResponse)
            assertSoftly {
                response.status shouldBe 202
                response.contentType shouldBe "application/json"
                response.charset shouldBe US_ASCII
            }

            every { applicationResponse.status() } returns null
            response.status shouldBe 200
        }
    }
}
