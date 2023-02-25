@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.server

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.*
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.Logbook
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.common.readBytes


@API(status = EXPERIMENTAL)
@ExperimentalLogbookKtorApi
class LogbookServer(
    val logbook: Logbook
) {

    class Config {
        var logbook: Logbook = Logbook.create()
    }

    companion object : BaseApplicationPlugin<Application, Config, LogbookServer> {
        private val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> =
            AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookServer> = AttributeKey("LogbookServer")
        override fun install(pipeline: Application, configure: Config.() -> Unit): LogbookServer {
            val config = Config().apply(configure)
            val plugin = LogbookServer(config.logbook)

            pipeline.receivePipeline.intercept(ApplicationReceivePipeline.Before) {
                val request = ServerRequest(call.request)
                val requestWritingStage = plugin.logbook.process(request)
                val proceedWith = when {
                    request.shouldBuffer() && !call.request.receiveChannel().isClosedForRead -> {
                        val content = call.request.receiveChannel().readBytes()
                        request.buffer(content)
                        ByteReadChannel(content)
                    }

                    else -> it
                }
                val responseProcessingStage = requestWritingStage.write()
                call.attributes.put(responseProcessingStageKey, responseProcessingStage)
                proceedWith(proceedWith)
            }

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                val responseProcessingStage = call.attributes[responseProcessingStageKey]
                val response = ServerResponse(call.response)
                val responseWritingStage = responseProcessingStage.process(response)
                val proceedWith = when {
                    response.shouldBuffer() -> {
                        val content = (it as OutgoingContent).readBytes(pipeline)
                        response.buffer(content)
                        ByteArrayContent(content)
                    }

                    else -> it
                }
                responseWritingStage.write()
                proceedWith(proceedWith)
            }

            return plugin
        }
    }
}