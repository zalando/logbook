package org.zalando.logbook.autoconfigure.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.http.client.autoconfigure.reactive.ClientHttpConnectorBuilderCustomizer;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;

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
