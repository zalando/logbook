@file:Suppress(
    "BlockingMethodInNonBlockingContext"
)

package org.zalando.logbook.server

import io.ktor.http.content.*
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.PluginBuilder
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.ApplicationReceivePipeline
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.routing.RoutingApplicationRequest
import io.ktor.util.*
import io.ktor.utils.io.*
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import org.zalando.logbook.Logbook
import org.zalando.logbook.common.ExperimentalLogbookKtorApi
import org.zalando.logbook.common.readBytes

@ExperimentalLogbookKtorApi
public val LogbookPlugin: ApplicationPlugin<LogbookConfig> = createApplicationPlugin("LogbookServer", ::LogbookConfig) {
    buildPlugin()
}

/*
public val RateLimit: ApplicationPlugin<RateLimitConfig> = createApplicationPlugin("RateLimit", ::RateLimitConfig) {
    val global = pluginConfig.global
    val providers = when {
        global != null -> pluginConfig.providers + (LIMITER_NAME_GLOBAL to global)
        else -> pluginConfig.providers.toMap()
    }
    check(providers.isNotEmpty()) { "At least one provider must be specified" }
    application.attributes.put(RateLimiterConfigsRegistryKey, providers)

    if (global == null) return@createApplicationPlugin
    application.install(RateLimitApplicationInterceptors) {
        this.providerNames = listOf(LIMITER_NAME_GLOBAL)
    }
}
 */

internal fun PluginBuilder<LogbookConfig>.buildPlugin() {
    val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> = AttributeKey("Logbook.ResponseProcessingStage")
    val logbook = pluginConfig.logbook
    application.receivePipeline.intercept(ApplicationReceivePipeline.Before) {
        val request = ServerRequest(call.request)
        val requestWritingStage = logbook.process(request)
        val proceedWith = when {
            request.shouldBuffer() && !call.request.receiveChannel().isClosedForRead -> {
                val content = call.request.receiveChannel().readBytes()
                request.buffer(content)
//                RoutingApplicationRequest(this.typeInfo, ByteReadChannel(content))
            }

            else -> it
        }
        val responseProcessingStage = requestWritingStage.write()
        call.attributes.put(responseProcessingStageKey, responseProcessingStage)
        proceedWith(proceedWith)
    }

    application.sendPipeline.intercept(ApplicationSendPipeline.Render) {
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


}

class LogbookConfig {
    val logbook: Logbook = Logbook.create()
}

@API(status = EXPERIMENTAL)
@ExperimentalLogbookKtorApi
class LogbookServer(
    val logbook: Logbook
) {

//    class LogbookConfig {
//        var logbook: Logbook = Logbook.create()
//    }
//
//

//    companion object : ApplicationPlugin<Config, LogbookServer> {
//        private val responseProcessingStageKey: AttributeKey<Logbook.ResponseProcessingStage> = AttributeKey("Logbook.ResponseProcessingStage")
//        override val key: AttributeKey<LogbookServer> = AttributeKey("LogbookServer")
//        override fun install(pipeline: Application, configure: Config.() -> Unit): LogbookServer {
//            val config = Config().apply(configure)
//            val feature = LogbookServer(config.logbook)
//
//            pipeline.receivePipeline.intercept(ApplicationReceivePipeline.Before) {
//                val request = ServerRequest(call.request)
//                val requestWritingStage = feature.logbook.process(request)
//                val proceedWith = when {
//                    request.shouldBuffer() && !call.request.receiveChannel().isClosedForRead -> {
//                        val content = call.request.receiveChannel().readBytes()
//                        request.buffer(content)
//                        ApplicationReceiveRequest(it.typeInfo, ByteReadChannel(content))
//                    }
//                    else -> it
//                }
//                val responseProcessingStage = requestWritingStage.write()
//                call.attributes.put(responseProcessingStageKey, responseProcessingStage)
//                proceedWith(proceedWith)
//            }
//
//            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Render) {
//                val responseProcessingStage = call.attributes[responseProcessingStageKey]
//                val response = ServerResponse(call.response)
//                val responseWritingStage = responseProcessingStage.process(response)
//                val proceedWith = when {
//                    response.shouldBuffer() -> {
//                        val content = (it as OutgoingContent).readBytes(pipeline)
//                        response.buffer(content)
//                        ByteArrayContent(content)
//                    }
//                    else -> it
//                }
//                responseWritingStage.write()
//                proceedWith(proceedWith)
//            }
//
//            return feature
//        }
//    }
}
