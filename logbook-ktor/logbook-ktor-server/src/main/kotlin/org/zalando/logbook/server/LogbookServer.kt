package org.zalando.logbook.server


import io.ktor.http.content.OutgoingContent.ByteArrayContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.doublereceive.DoubleReceive
import io.ktor.server.request.receiveChannel
import io.ktor.util.AttributeKey
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.Logbook
import org.zalando.logbook.Logbook.ResponseProcessingStage
import org.zalando.logbook.common.readBytes

class LogbookServerConfiguration {
    var logbook: Logbook = Logbook.create()
}

@API(status = EXPERIMENTAL)
val LogbookServer = createApplicationPlugin(
    name = "LogbookServerPlugin",
    createConfiguration = ::LogbookServerConfiguration,
) {

    application.install(DoubleReceive)

    val responseProcessingStageKey: AttributeKey<ResponseProcessingStage> =
        AttributeKey("Logbook.ResponseProcessingStage")

    onCall { call ->
        val request = ServerRequest(call.request)
        val requestWritingStage = pluginConfig.logbook.process(request)
        if (request.shouldBuffer()) {
            request.buffer(call.receiveChannel().readBytes())
        }
        val responseProcessingStage = requestWritingStage.write()
        call.attributes.put(responseProcessingStageKey, responseProcessingStage)
    }

    onCallRespond { call, body ->
        handleCallRespond(call, body, responseProcessingStageKey)
    }
}

/**
 * This function is extracted and marked as `suspend` to satisfy JaCoCo test coverage.
 * For more info, see
 * [this comment](https://github.com/zalando/logbook/pull/1819#issuecomment-2097583993).
 */
@Suppress("RedundantSuspendModifier")
private suspend fun handleCallRespond(
    call: ApplicationCall,
    body: Any,
    responseProcessingStageKey: AttributeKey<ResponseProcessingStage>
) {
    val responseProcessingStage = call.attributes[responseProcessingStageKey]
    val response = ServerResponse(call.response, body)
    val responseWritingStage = responseProcessingStage.process(response)
    if (response.shouldBuffer() && body is ByteArrayContent) {
        response.buffer(body.bytes())
    }
    responseWritingStage.write()
}
