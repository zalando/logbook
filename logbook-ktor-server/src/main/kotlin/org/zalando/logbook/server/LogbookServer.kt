@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.server

import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
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

    companion object : ApplicationFeature<Application, Config, LogbookServer> {
        private val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> = AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookServer> = AttributeKey("LogbookServer")
        override fun install(pipeline: Application, configure: Config.() -> Unit): LogbookServer {
            val config = Config().apply(configure)
            val feature = LogbookServer(config.logbook)

            pipeline.receivePipeline.intercept(ApplicationReceivePipeline.Before) {
                val request = ServerRequest(call.request)
                val requestWritingStage = feature.logbook.process(request)
                val req = when {
                    request.shouldBuffer() && call.request.receiveChannel().availableForRead > 0 -> {
                        val content = call.request.receiveChannel().readBytes()
                        request.buffer(content)
                        ApplicationReceiveRequest(it.typeInfo, ByteReadChannel(content))
                    }
                    else -> it
                }
                val responseProcessingStage = requestWritingStage.write()
                call.attributes.put(responseProcessingStageKey, responseProcessingStage)
                proceedWith(req)
            }

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
                val responseProcessingStage = call.attributes[responseProcessingStageKey]
                val response = ServerResponse(call.response)
                val responseWritingStage = responseProcessingStage.process(response)
                if (response.shouldBuffer()) {
                    val content = (subject as OutgoingContent).readBytes(pipeline)
                    response.buffer(content)
                }
                responseWritingStage.write()
                proceed()
            }

            return feature
        }
    }
}