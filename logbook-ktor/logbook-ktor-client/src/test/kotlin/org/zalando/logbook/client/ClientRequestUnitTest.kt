package org.zalando.logbook.client

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.text.Charsets.US_ASCII
import org.zalando.logbook.ContentType.CONTENT_TYPE_HEADER

internal class ClientRequestUnitTest {

    @Test
    fun `ClientRequest unit test`() {
        val req = HttpRequestBuilder().apply {
            headers.append(HttpHeaders.ContentType, "application/json; charset=us-ascii")
        }
        val request = ClientRequest(req)
        assertThat(request.contentType).isEqualTo("application/json")
        assertThat(request.charset).isEqualTo(US_ASCII)
    }

    @Test
    fun `should get content type from request body if not present in headers`() {
        val req = HttpRequestBuilder().apply {
            setBody(
                object : OutgoingContent.ByteArrayContent() {
                    override val contentType = io.ktor.http.ContentType.Application.Json
                    override val contentLength = 0L
                    override fun bytes() = ByteArray(0)
                }
            )
        }
        val request = ClientRequest(req)
        assertThat(request.contentType).isEqualTo("application/json")
        assertThat(request.headers.getFirst(CONTENT_TYPE_HEADER)).isEqualTo("application/json")
    }

    @Test
    fun `should not get content type if request body is not OutgoingContent`() {
        val req = HttpRequestBuilder().apply {
            setBody("test")
        }
        val request = ClientRequest(req)
        assertThat(request.contentType).isNull()
        assertThat(request.headers.getFirst(CONTENT_TYPE_HEADER)).isNull()
    }

    @Test
    fun `should not get content type if there is no Content-Type header`() {
        val req = HttpRequestBuilder().apply {
            setBody(
                    object : OutgoingContent.ByteArrayContent() {
                        override val contentLength = 0L
                        override fun bytes() = ByteArray(0)
                    }
            )
        }
        val request = ClientRequest(req)
        assertThat(request.contentType).isNull()
        assertThat(request.headers.getFirst(CONTENT_TYPE_HEADER)).isNull()
    }

    @Test
    fun `should use content type from headers if present`() {
        val req = HttpRequestBuilder().apply {
            setBody(
                    object : OutgoingContent.ByteArrayContent() {
                        override val contentType = io.ktor.http.ContentType.Application.Json
                        override val contentLength = 0L
                        override fun bytes() = ByteArray(0)
                    }
            )
            header(HttpHeaders.ContentType, "application/xml")
        }
        val request = ClientRequest(req)
        assertThat(request.contentType).isEqualTo("application/xml")
        assertThat(request.headers.getFirst(CONTENT_TYPE_HEADER)).isEqualTo("application/xml")
    }
}
