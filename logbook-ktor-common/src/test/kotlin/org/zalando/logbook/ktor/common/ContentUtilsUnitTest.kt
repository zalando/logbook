package org.zalando.logbook.ktor.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.content.ByteArrayContent
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.zalando.logbook.common.EMPTY_BODY
import org.zalando.logbook.common.readBytes
import kotlin.coroutines.CoroutineContext
import kotlin.text.Charsets.UTF_8
import kotlin.text.toByteArray


internal class ContentUtilsUnitTest : FunSpec() {

    private val scope = CoroutineScope(Job())
    private val expected = "foobar"

    init {
        test("should read NoContent") {
            val content = object : OutgoingContent.NoContent() {}
            val result = content.readBytes(scope)
            result shouldBe EMPTY_BODY
        }

        test("should read ProtocolUpgrade") {
            val content = object : OutgoingContent.ProtocolUpgrade() {
                override suspend fun upgrade(
                    input: ByteReadChannel,
                    output: ByteWriteChannel,
                    engineContext: CoroutineContext,
                    userContext: CoroutineContext
                ): Job = Job()
            }
            val result = content.readBytes(scope)
            result shouldBe EMPTY_BODY
        }

        test("should read ByteArrayContent") {
            val content = ByteArrayContent(expected.toByteArray())
            val result = content.readBytes(scope).toString(UTF_8)
            result shouldBe expected
        }

        test("should read ReadChannelContent") {
            val content = object : OutgoingContent.ReadChannelContent() {
                override fun readFrom(): ByteReadChannel = ByteReadChannel(expected.toByteArray())
            }
            val result = content.readBytes(scope).toString(UTF_8)
            result shouldBe expected
        }

        test("should read WriteChannelContent") {
            val content = object : OutgoingContent.WriteChannelContent() {
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    channel.writeFully(expected.toByteArray())
                }

            }
            val result = content.readBytes(scope).toString(UTF_8)
            result shouldBe expected
        }

        test("should return fallback value") {
            val content = object : OutgoingContent.ReadChannelContent() {
                override fun readFrom(): ByteReadChannel = throw IllegalArgumentException()
            }
            val bytes = content.readBytes(scope)
            bytes shouldBe EMPTY_BODY
        }

        test("should return fallback value from ByteReadChannel") {
            val delegate = ByteReadChannel(expected.toByteArray())
            val content = object : ByteReadChannel by delegate {
                override suspend fun readRemaining(limit: Long): ByteReadPacket =
                    throw IllegalArgumentException()
            }
            val bytes = content.readBytes()
            bytes shouldBe EMPTY_BODY
        }
    }
}
