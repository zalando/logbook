package org.zalando.logbook.spring.webflux;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookClientHandler;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientConfig;
import reactor.netty.transport.ClientTransportConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogbookNettyClientCustomizerTest {

    @InjectMocks
    private LogbookNettyClientCustomizer logbookNettyClientCustomizer;
    @Mock
    private Logbook mockLogbook;
    @Mock
    private Connection mockConnection;

    @Test
    void shouldAddLogbookClientHandler() {
        ReactorClientHttpConnectorBuilder reactorClientHttpConnectorBuilder = ClientHttpConnectorBuilder.reactor();

        ReactorClientHttpConnectorBuilder reactorClientHttpConnectorBuilderActual = logbookNettyClientCustomizer.customize(reactorClientHttpConnectorBuilder);
        ReactorClientHttpConnector reactorClientHttpConnectorActual = reactorClientHttpConnectorBuilderActual.build();

        Assertions.assertThat(reactorClientHttpConnectorBuilderActual)
                .isNotNull()
                .isNotSameAs(reactorClientHttpConnectorBuilder);
        Assertions.assertThat(reactorClientHttpConnectorActual)
                .extracting("httpClient", InstanceOfAssertFactories.type(HttpClient.class))
                .extracting(HttpClient::configuration, InstanceOfAssertFactories.type(HttpClientConfig.class))
                .extracting(ClientTransportConfig::doOnConnected)
                .isNotNull()
                .satisfies(connectionConsumer -> {
                    connectionConsumer.accept(mockConnection);
                    verify(mockConnection).addHandlerLast(any(LogbookClientHandler.class));
                });
    }
}