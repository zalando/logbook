@file:Suppress(
    "SimpleRedundantLet" // jacoco workaround
)

package org.zalando.logbook.client

import io.ktor.http.*
import io.ktor.util.*
import org.zalando.logbook.HttpHeaders
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Origin
import org.zalando.logbook.common.State
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8
import io.ktor.client.statement.HttpResponse as KtorResponse


internal class ClientResponse(
    private val response: KtorResponse
) : HttpResponse {
    private val state: AtomicReference<State> = AtomicReference(State.Unbuffered)

    override fun getProtocolVersion(): String = response.version.toString()
    override fun getOrigin(): Origin = Origin.REMOTE
    override fun getHeaders(): HttpHeaders = HttpHeaders.of(response.headers.toMap())
    override fun getContentType(): String? = response.contentType()?.let { it.toString().substringBefore(";") }
    override fun getCharset(): Charset = response.charset() ?: UTF_8
    override fun getStatus(): Int = response.status.value
    override fun withBody(): HttpResponse = apply { state.updateAndGet { it.with() } }
    override fun withoutBody(): HttpResponse = apply { state.updateAndGet { it.without() } }
    override fun getBody(): ByteArray = state.get().body
    internal fun buffer(bytes: ByteArray) = state.updateAndGet { it.buffer(bytes) }
    internal fun shouldBuffer(): Boolean = state.get() is State.Offering
}