package org.zalando.logbook.server

import io.ktor.http.*
import io.ktor.http.ContentType.Companion.parse
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.request.*
import io.ktor.util.*
import org.zalando.logbook.HttpHeaders
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.Origin
import org.zalando.logbook.common.State
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8


internal class ServerRequest(
    private val request: ApplicationRequest
) : HttpRequest {
    private val state: AtomicReference<State> = AtomicReference(State.Unbuffered)

    override fun getProtocolVersion(): String = request.httpVersion
    override fun getOrigin(): Origin = Origin.REMOTE
    override fun getHeaders(): HttpHeaders = HttpHeaders.of(request.headers.toMap())
    override fun getContentType(): String? = request.contentType?.contentType
    override fun getCharset(): Charset = request.contentType?.charset() ?: UTF_8
    override fun getRemote(): String = request.local.remoteHost
    override fun getMethod(): String = request.httpMethod.value
    override fun getScheme(): String = request.local.scheme
    override fun getHost(): String = request.host()
    override fun getPort(): Optional<Int> = Optional.of(request.port())
    override fun getPath(): String = request.path()
    override fun getQuery(): String = request.queryString()
    override fun getNativeRequest(): Any = request
    override fun withBody(): HttpRequest = apply { state.updateAndGet { it.with() } }
    override fun withoutBody(): HttpRequest = apply { state.updateAndGet { it.without() } }
    override fun getBody(): ByteArray = state.get().body
    internal fun buffer(bytes: ByteArray): State = state.updateAndGet { it.buffer(bytes) }
    internal fun shouldBuffer(): Boolean = state.get() is State.Offering

    private val ApplicationRequest.contentType: ContentType? get() = headers[(ContentType)]?.let { parse(it) }
}