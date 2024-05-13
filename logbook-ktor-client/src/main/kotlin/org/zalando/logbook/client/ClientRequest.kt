@file:Suppress(
    "SimpleRedundantLet", // jacoco workaround
)

package org.zalando.logbook.client

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.http.HttpProtocolVersion.Companion.HTTP_1_1
import io.ktor.http.charset
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.util.toMap
import kotlin.text.Charsets.UTF_8
import org.zalando.logbook.ContentType.CONTENT_TYPE_HEADER
import org.zalando.logbook.HttpHeaders
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.Origin
import org.zalando.logbook.common.State
import java.nio.charset.Charset
import java.util.Optional
import java.util.concurrent.atomic.AtomicReference

internal class ClientRequest(
    private val request: HttpRequestBuilder,
) : HttpRequest {
    private val state: AtomicReference<State> = AtomicReference(State.Unbuffered)

    override fun getProtocolVersion(): String = HTTP_1_1.toString()
    override fun getOrigin(): Origin = Origin.LOCAL
    override fun getHeaders(): HttpHeaders = HttpHeaders.of(request.headers.build().toMap()).also {
        // as Ktor removes the Content-Type header from the headers in the default transformers we need to add it back
        // https://github.com/ktorio/ktor/blob/e789b015bd5169505df1c59ced0d2690026af523/ktor-client/ktor-client-core/common/src/io/ktor/client/plugins/DefaultTransform.kt#L55
        if (it.contains(CONTENT_TYPE_HEADER).not() && request.body is OutgoingContent) return it.update(
            CONTENT_TYPE_HEADER, (request.body as OutgoingContent).contentType?.toString()
        )
    }

    override fun getContentType(): String? = (request.contentType() ?: (request.body as? OutgoingContent)?.contentType)
            ?.let { it.toString().substringBefore(";") }
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
