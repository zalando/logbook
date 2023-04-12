@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.server

import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.log
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import org.zalando.logbook.Logbook
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.common.readBytes

@ExperimentalLogbookKtorApi
val LogbookServer: ApplicationPlugin<LogbookConfig> = createApplicationPlugin("LogbookServer", ::LogbookConfig) {
    val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> = AttributeKey("Logbook.ResponseProcessingStage")
    onCallReceive { call ->
        val request = ServerRequest(call.request)
        val requestWritingStage = pluginConfig.logbook.process(request)
        transformBody {
            val proceedWith = when {
                request.shouldBuffer() && !it.isClosedForRead -> {
                    val content = it.readBytes()
                    request.buffer(content)
                    ByteReadChannel(content)
                }

                else -> it
            }
            val responseProcessingStage = requestWritingStage.write()
            call.attributes.put(responseProcessingStageKey, responseProcessingStage)
            proceedWith
        }
    }
    onCallRespond { call ->
        val responseProcessingStage = call.attributes.getOrNull(responseProcessingStageKey)
        call.application.log.warn("Logbook could not call transformBody() in onCallReceive interceptor. Skipping response processing... ")
        if (responseProcessingStage != null) {
            val response = ServerResponse(call.response)
            val responseWritingStage = responseProcessingStage.process(response)
            transformBody { data ->
                val proceedWith = when {
                    response.shouldBuffer() -> {
                        if (data is OutgoingContent) {
                            val content = data.readBytes(call.application)
                            response.buffer(content)
                            ByteArrayContent(content)
                        } else data
                    }

                    else -> data
                }
                responseWritingStage.write()
                proceedWith
            }
        }
    }
}

class LogbookConfig {
    var logbook: Logbook = Logbook.create()
}
