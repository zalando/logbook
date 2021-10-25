@file:Suppress(
    "EXPERIMENTAL_API_USAGE_FUTURE_ERROR"
)

package org.zalando.logbook.client

import io.ktor.client.request.*
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.text.Charsets.US_ASCII


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
}