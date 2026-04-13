package org.zalando.logbook.server

import io.ktor.http.HeadersImpl
import io.ktor.http.HttpHeaders
import io.ktor.server.request.ApplicationRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ServerRequestTest {

    @Test
    fun `Should return content type`() {
        val applicationRequest = mock(ApplicationRequest::class.java)
        `when`(applicationRequest.headers)
                .thenReturn(HeadersImpl(mapOf(HttpHeaders.ContentType to listOf("application/json"))))

        val req = ServerRequest(applicationRequest)

        Assertions.assertThat(req.contentType)
                .isEqualTo("application/json")
    }
}
