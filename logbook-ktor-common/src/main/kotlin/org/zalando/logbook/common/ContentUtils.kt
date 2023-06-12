package org.zalando.logbook.common

import io.ktor.http.content.OutgoingContent
import io.ktor.util.toByteArray
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@JvmField
internal val EMPTY_BODY = ByteArray(0)

suspend fun OutgoingContent.readBytes(scope: CoroutineScope): ByteArray = runCatching {
    when (this) {
        is OutgoingContent.NoContent -> EMPTY_BODY
        is OutgoingContent.ProtocolUpgrade -> EMPTY_BODY
        is OutgoingContent.ByteArrayContent -> bytes()
        is OutgoingContent.ReadChannelContent -> readFrom().readBytes()
        is OutgoingContent.WriteChannelContent -> scope.writer(Dispatchers.Unconfined) { writeTo(channel) }.channel.readBytes()
    }
}.getOrElse {
    EMPTY_BODY
}

suspend fun ByteReadChannel.readBytes(): ByteArray = runCatching {
    toByteArray()
}.getOrElse {
    EMPTY_BODY
}
