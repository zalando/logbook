package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.http.client.autoconfigure.reactive.ClientHttpConnectorBuilderCustomizer;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;

/**
 * {@link ClientHttpConnectorBuilderCustomizer} to add a {@link LogbookClientHandler} to the Netty HTTP client.
 */
@RequiredArgsConstructor
public class LogbookNettyClientCustomizer implements ClientHttpConnectorBuilderCustomizer<ReactorClientHttpConnectorBuilder> {

    private final Logbook logbook;

    @Override
    public ReactorClientHttpConnectorBuilder customize(ReactorClientHttpConnectorBuilder builder) {
        return builder.withHttpClientCustomizer(httpClient ->
                httpClient.doOnConnected(connection ->
                        connection.addHandlerLast(new LogbookClientHandler(logbook)))
        );
    }

}
