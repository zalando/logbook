@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.client

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import io.ktor.util.*
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.Logbook
import org.zalando.logbook.Logbook.ResponseProcessingStage
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.common.readBytes


@API(status = EXPERIMENTAL)
@ExperimentalLogbookKtorApi
class LogbookClient(
    val logbook: Logbook
) {

    class Config {
        var logbook: Logbook = Logbook.create()
    }

    companion object : HttpClientFeature<Config, LogbookClient> {
        private val responseProcessingStageKey: AttributeKey<ResponseProcessingStage> = AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookClient> = AttributeKey("LogbookFeature")
        override fun prepare(block: Config.() -> Unit): LogbookClient = LogbookClient(Config().apply(block).logbook)
        override fun install(feature: LogbookClient, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
                val request = ClientRequest(context)
                val requestWritingStage = feature.logbook.process(request)
                if (request.shouldBuffer()) {
                    val content = (context.body as OutgoingContent).readBytes(scope)
                    request.buffer(content)
                }
                val responseStage = requestWritingStage.write()
                context.attributes.put(responseProcessingStageKey, responseStage)
                proceed()
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.After) {
                val (loggingContent, responseContent) = it.content.split(it)

                val responseProcessingStage = it.call.attributes[responseProcessingStageKey]
                val response = ClientResponse(it)
                val responseWritingStage = responseProcessingStage.process(response)
                if (response.shouldBuffer() && !loggingContent.isClosedForRead) {
                    val content = loggingContent.readBytes()
                    response.buffer(content)
                }
                responseWritingStage.write()

                val newClientCall = context.wrapWithContent(responseContent)
                proceedWith(newClientCall.response)
            }
        }
    }
}