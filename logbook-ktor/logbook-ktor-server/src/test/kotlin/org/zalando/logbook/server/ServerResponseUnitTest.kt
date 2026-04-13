package org.zalando.logbook.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.ApplicationResponse
import io.ktor.server.response.ResponseHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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

        val response = ServerResponse(resp, HttpStatusCode.MovedPermanently)
        assertThat(response.status).isEqualTo(202)
        assertThat(response.contentType).isEqualTo("application/json")
        assertThat(response.charset).isEqualTo(US_ASCII)
    }

    @Test
    fun `should extract status from body if response status is null`() {
        val resp = mock(ApplicationResponse::class.java)
        `when`(resp.status()).thenReturn(null)

        val response = ServerResponse(resp, HttpStatusCode.NotFound)
        assertThat(response.status).isEqualTo(404)
    }

    @Test
    fun `should set default status to 200 if body is not HttpStatusCode and response status is null`() {
        val resp = mock(ApplicationResponse::class.java)
        `when`(resp.status()).thenReturn(null)

        val response = ServerResponse(resp, "Hello, world!")
        assertThat(response.status).isEqualTo(200)
    }
}
