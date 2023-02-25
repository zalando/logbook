package org.zalando.logbook.server

import io.ktor.http.*
import io.ktor.server.response.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.text.Charsets.US_ASCII


internal class ServerResponseUnitTest {

    @Test
    fun `ServerResponse unit test`() {
        val resp = mock(ApplicationResponse::class.java)
        `when`(resp.status()).thenReturn(HttpStatusCode.Accepted)
        `when`(resp.headers).thenReturn(object : ResponseHeaders() {
            override fun engineAppendHeader(name: String, value: String) = Unit
            override fun getEngineHeaderNames(): List<String> = listOf(HttpHeaders.ContentType)
            override fun getEngineHeaderValues(name: String): List<String> =
                listOf("application/json; charset=us-ascii")
        })

        val response = ServerResponse(resp)
        assertThat(response.status).isEqualTo(202)
        assertThat(response.contentType).isEqualTo("application/json")
        assertThat(response.charset).isEqualTo(US_ASCII)

        `when`(resp.status()).thenReturn(null)
        assertThat(response.status).isEqualTo(200)
    }
}