@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.client

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
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

                val content = (it as OutgoingContent)
                content.contentLength?.let { length ->
                    request.addContentHeader(
                        HttpHeaders.ContentLength,
                        length.toString()
                    )
                }
                content.contentType?.let { type ->
                    request.addContentHeader(
                        HttpHeaders.ContentType,
                        type.toString()
                    )
                }

                val requestWritingStage = feature.logbook.process(request)

                val proceedWith = when {
                    request.shouldBuffer() -> {
                        val content = content.readBytes(scope)
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