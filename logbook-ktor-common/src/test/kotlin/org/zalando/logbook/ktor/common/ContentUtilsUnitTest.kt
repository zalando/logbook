package org.zalando.logbook.ktor.common

import io.ktor.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.zalando.logbook.common.EMPTY_BODY
import org.zalando.logbook.common.readBytes
import kotlin.coroutines.CoroutineContext
import kotlin.text.Charsets.UTF_8

internal class ContentUtilsUnitTest {

    private val scope = CoroutineScope(Job())
    private val expected = "foobar"

    @Test
    fun `Should read NoContent`() = runBlocking {
        val content = object : OutgoingContent.NoContent() {}
        val result = content.readBytes(scope)
        assertEquals(EMPTY_BODY, result)
    }

    @Test
    fun `Should read ProtocolUpgrade`() = runBlocking {
        val content = object : OutgoingContent.ProtocolUpgrade() {
            override suspend fun upgrade(
                input: ByteReadChannel,
                output: ByteWriteChannel,
                engineContext: CoroutineContext,
                userContext: CoroutineContext,
            ): Job = Job()
        }
        val result = content.readBytes(scope)
        assertEquals(EMPTY_BODY, result)
    }

    @Test
    fun `Should read ByteArrayContent`() = runBlocking {
        val content = ByteArrayContent(expected.toByteArray())
        val result = content.readBytes(scope).toString(UTF_8)
        assertEquals(expected, result)
    }

    @Test
    fun `Should read ReadChannelContent`() = runBlocking {
        val content = object : OutgoingContent.ReadChannelContent() {
            override fun readFrom(): ByteReadChannel = ByteReadChannel(expected.toByteArray())
        }
        val result = content.readBytes(scope).toString(UTF_8)
        assertEquals(expected, result)
    }

    @Test
    fun `Should read WriteChannelContent`() = runBlocking {
        val content = object : OutgoingContent.WriteChannelContent() {
            override suspend fun writeTo(channel: ByteWriteChannel) {
                channel.writeFully(expected.toByteArray())
            }
        }
        val result = content.readBytes(scope).toString(UTF_8)
        assertEquals(expected, result)
    }

    @Test
    fun `Should return fallback value`() = runBlocking {
        val content = object : OutgoingContent.ReadChannelContent() {
            override fun readFrom(): ByteReadChannel = throw IllegalArgumentException()
        }
        val bytes = content.readBytes(scope)
        assertEquals(EMPTY_BODY, bytes)
    }

    @Test
    fun `Should return fallback value from ByteReadChannel`() = runBlocking {
        val delegate = ByteReadChannel(expected.toByteArray())
        val content = object : ByteReadChannel by delegate {
            override suspend fun readRemaining(limit: Long): ByteReadPacket =
                throw IllegalArgumentException()
        }
        val bytes = content.readBytes()
        assertEquals(EMPTY_BODY, bytes)
    }
}
