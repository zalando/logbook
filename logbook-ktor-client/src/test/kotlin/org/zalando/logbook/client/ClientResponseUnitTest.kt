package org.zalando.logbook.client

import io.ktor.client.statement.*
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.text.Charsets.UTF_8

internal class ClientResponseUnitTest {

    @Test
    fun `ClientResponse unit test`() {
        val resp = mock(HttpResponse::class.java)
        val response = ClientResponse(resp)
        `when`(resp.headers).thenReturn(headersOf())
        assertThat(response.contentType).isNull()
        assertThat(response.charset).isEqualTo(UTF_8)
    }
}
