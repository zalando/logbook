@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.client

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.observer.wrapWithContent
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.util.split
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.api.Logbook
import org.zalando.logbook.api.Logbook.ResponseProcessingStage
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
                val proceedWith = when {
                    request.shouldBuffer() -> {
                        val content = (it as OutgoingContent).readBytes(scope)
                        request.buffer(content)
                        ByteArrayContent(content)
                    }
                    else -> it
                }
                val responseStage = requestWritingStage.write()
                context.attributes.put(responseProcessingStageKey, responseStage)
                proceedWith(proceedWith)
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

                val proceedWith = context.wrapWithContent(responseContent).response
                proceedWith(proceedWith)
            }
        }
    }
}
