@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
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

    companion object : HttpClientPlugin<Config, LogbookClient> {
        private val responseProcessingStageKey: AttributeKey<ResponseProcessingStage> =
            AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookClient> = AttributeKey("LogbookFeature")
        override fun prepare(block: Config.() -> Unit): LogbookClient = LogbookClient(Config().apply(block).logbook)
        @OptIn(InternalAPI::class)
        override fun install(plugin: LogbookClient, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
                val request = ClientRequest(context)
                val requestWritingStage = plugin.logbook.process(request)
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

            scope.receivePipeline.intercept(HttpReceivePipeline.After) { httpResponse ->
                val (loggingContent, responseContent) = httpResponse.content.split(httpResponse)

                val responseProcessingStage = httpResponse.call.attributes[responseProcessingStageKey]
                val clientResponse = ClientResponse(httpResponse)
                val responseWritingStage = responseProcessingStage.process(clientResponse)
                if (clientResponse.shouldBuffer() && !loggingContent.isClosedForRead) {
                    val content = loggingContent.readBytes()
                    clientResponse.buffer(content)
                }
                responseWritingStage.write()

                val proceedWith = context.wrapWithContent(responseContent).response
                proceedWith(proceedWith)
            }
        }
    }
}