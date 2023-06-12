@file:Suppress(
    "SimpleRedundantLet", // jacoco workaround
)

package org.zalando.logbook.client

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.http.HttpProtocolVersion.Companion.HTTP_1_1
import io.ktor.http.charset
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.util.toMap
import org.zalando.logbook.HttpHeaders
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.Origin
import org.zalando.logbook.common.State
import java.nio.charset.Charset
import java.util.Optional
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8

internal class ClientRequest(
    private val request: HttpRequestBuilder,
) : HttpRequest {
    private val state: AtomicReference<State> = AtomicReference(State.Unbuffered)

    override fun getProtocolVersion(): String = HTTP_1_1.toString()
    override fun getOrigin(): Origin = Origin.LOCAL
    override fun getHeaders(): HttpHeaders = HttpHeaders.of(request.headers.build().toMap())
    override fun getContentType(): String? = request.contentType()?.let { it.toString().substringBefore(";") }
    override fun getCharset(): Charset = request.charset() ?: UTF_8
    override fun getRemote(): String = "localhost"
    override fun getMethod(): String = request.method.value
    override fun getScheme(): String = request.url.protocol.name
    override fun getHost(): String = request.host
    override fun getPort(): Optional<Int> = Optional.of(request.port)
    override fun getPath(): String = request.url.encodedPath
    override fun getQuery(): String = request.url.buildString().substringAfter("?", "")
    override fun withBody(): HttpRequest = apply { state.updateAndGet { it.with() } }
    override fun withoutBody(): HttpRequest = apply { state.updateAndGet { it.without() } }
    override fun getBody(): ByteArray = state.get().body
    internal fun buffer(bytes: ByteArray): State = state.updateAndGet { it.buffer(bytes) }
    internal fun shouldBuffer(): Boolean = state.get() is State.Offering
}
