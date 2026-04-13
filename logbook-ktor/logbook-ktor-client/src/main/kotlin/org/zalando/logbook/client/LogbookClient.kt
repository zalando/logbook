@file:Suppress(
    "BlockingMethodInNonBlockingContext",
)

package org.zalando.logbook.client

import io.ktor.client.HttpClient
import io.ktor.client.call.replaceResponse
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.launch
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.Logbook
import org.zalando.logbook.Logbook.ResponseProcessingStage
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.common.readBytes

@API(status = EXPERIMENTAL)
@ExperimentalLogbookKtorApi
class LogbookClient(
    val logbook: Logbook,
) {

    class Config {
        var logbook: Logbook = Logbook.create()
    }

    companion object : HttpClientPlugin<Config, LogbookClient> {
        private val responseProcessingStageKey: AttributeKey<ResponseProcessingStage> =
            AttributeKey("Logbook.ResponseProcessingStage")
        override val key: AttributeKey<LogbookClient> = AttributeKey("LogbookPlugin")
        override fun prepare(block: Config.() -> Unit): LogbookClient = LogbookClient(Config().apply(block).logbook)

        @OptIn(InternalAPI::class)
        override fun install(plugin: LogbookClient, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.Monitoring) {
                val request = ClientRequest(context)
                val requestWritingStage = plugin.logbook.process(request)

                if (request.shouldBuffer()) {
                    val content = (it as OutgoingContent).readBytes(scope)
                    request.buffer(content)
                }

                val responseStage = requestWritingStage.write()
                context.attributes.put(responseProcessingStageKey, responseStage)
                proceedWith(it)
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.After) { httpResponse ->
                val responseBody = httpResponse.rawContent.readBytes()
                scope.launch {
                    val responseProcessingStage = httpResponse.call.attributes[responseProcessingStageKey]
                    val clientResponse = ClientResponse(httpResponse)
                    val responseWritingStage = responseProcessingStage.process(clientResponse)
                    if (clientResponse.shouldBuffer()) {
                        clientResponse.buffer(responseBody)
                    }
                    responseWritingStage.write()
                }
                val proceedWith = httpResponse.call.replaceResponse { ByteReadChannel(responseBody) }.response
                proceedWith(proceedWith)
            }
        }
    }
}
