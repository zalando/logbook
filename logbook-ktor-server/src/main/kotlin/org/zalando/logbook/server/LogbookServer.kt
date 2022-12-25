@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.server

import io.ktor.application.Application
import io.ktor.application.*
import io.ktor.features.DoubleReceive
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
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
        private val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> =
            AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookServer> = AttributeKey("LogbookServer")
        override fun install(pipeline: Application, configure: Config.() -> Unit): LogbookServer {
            if (pipeline.featureOrNull(DoubleReceive) == null) {
                throw IllegalStateException("Logging request payload with Logbook requires DoubleReceive feature in application configuration")
            }

            val config = Config().apply(configure)
            val feature = LogbookServer(config.logbook)

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                val request = ServerRequest(call.request)
                val requestWritingStage = feature.logbook.process(request)
                if (request.shouldBuffer() && !call.request.receiveChannel().isClosedForRead) {
                    val content = call.receive<ByteArray>()
                    request.buffer(content)
                }
                val responseProcessingStage = requestWritingStage.write()
                call.attributes.put(responseProcessingStageKey, responseProcessingStage)
                proceed()
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

            return feature
        }
    }
}