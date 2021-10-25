@file:Suppress(
    "SimpleRedundantLet" // jacoco workaround
)

package org.zalando.logbook.server

import io.ktor.http.*
import io.ktor.http.ContentType.Companion.parse
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import org.zalando.logbook.HttpHeaders
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Origin
import org.zalando.logbook.common.State
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8



internal class ServerResponse(
    private val response: ApplicationResponse
) : HttpResponse {
    private val state: AtomicReference<State> = AtomicReference(State.Unbuffered)

    override fun getProtocolVersion(): String = response.call.request.httpVersion
    override fun getOrigin(): Origin = Origin.LOCAL
    override fun getHeaders(): HttpHeaders = HttpHeaders.of(response.headers.allValues().toMap())
    override fun getContentType(): String? = response.contentType?.let { it.toString().substringBefore(";") }
    override fun getCharset(): Charset = response.contentType?.charset() ?: UTF_8
    override fun getStatus(): Int = response.status()?.value ?: 200
    override fun getNativeResponse(): Any = response
    override fun withBody(): HttpResponse = apply { state.updateAndGet { it.with() } }
    override fun withoutBody(): HttpResponse = apply { state.updateAndGet { it.without() } }
    override fun getBody(): ByteArray = state.get().body
    internal fun buffer(bytes: ByteArray) = state.updateAndGet { it.buffer(bytes) }
    internal fun shouldBuffer(): Boolean = state.get() is State.Offering

    private val ApplicationResponse.contentType: ContentType? get() = headers[ContentType]?.let { parse(it) }
}