package org.zalando.logbook.autoconfigure.webflux;

import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.http.client.autoconfigure.reactive.ClientHttpConnectorBuilderCustomizer;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.springframework.util.ClassUtils;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;

@RequiredArgsConstructor
public class LogbookNettyClientCustomizer implements ClientHttpConnectorBuilderCustomizer<ReactorClientHttpConnectorBuilder> {

    // Micrometer tracing brings this along; without it there is no context to restore
    private static final boolean CONTEXT_PROPAGATION_PRESENT = ClassUtils.isPresent(
            "io.micrometer.context.ContextSnapshotFactory",
            LogbookNettyClientCustomizer.class.getClassLoader());

    // the name the handler has always had in the pipeline, kept so that looking it up still works
    private static final String HANDLER_NAME = LogbookClientHandler.class.getSimpleName();

    private final Logbook logbook;

    @Override
    public ReactorClientHttpConnectorBuilder customize(ReactorClientHttpConnectorBuilder builder) {
        return builder.withHttpClientCustomizer(httpClient ->
                httpClient.doOnConnected(connection ->
                        connection.addHandlerLast(HANDLER_NAME, createHandler(logbook, CONTEXT_PROPAGATION_PRESENT)))
        );
    }

    /**
     * Wraps the handler so that outgoing entries are logged with the trace id of the request that caused them.
     */
    static ChannelHandler createHandler(final Logbook logbook, final boolean contextPropagationPresent) {
        final LogbookClientHandler handler = new LogbookClientHandler(logbook);
        return contextPropagationPresent ? new ContextPropagatingHandler(handler) : handler;
    }
}
