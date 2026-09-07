package org.zalando.logbook.autoconfigure.webflux;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.zalando.logbook.Logbook;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientConfig;

import static org.assertj.core.api.Assertions.assertThat;

class LogbookNettyClientCustomizerTest {

    private final LogbookNettyClientCustomizer logbookNettyClientCustomizer =
            new LogbookNettyClientCustomizer(Logbook.create());

    @Test
    void shouldCustomizeUsingObserveWithoutRelyingOnDoOnConnected() {
        ReactorClientHttpConnector baseConnector = ClientHttpConnectorBuilder.reactor().build();
        ReactorClientHttpConnectorBuilder builder = ClientHttpConnectorBuilder.reactor();

        ReactorClientHttpConnectorBuilder customizedBuilder = logbookNettyClientCustomizer.customize(builder);
        ReactorClientHttpConnector customizedConnector = customizedBuilder.build();

        HttpClientConfig baseConfig = httpClientConfig(baseConnector);
        HttpClientConfig customizedConfig = httpClientConfig(customizedConnector);

        assertThat(customizedBuilder).isNotSameAs(builder);
        assertThat(customizedConfig.doOnConnected()).isEqualTo(baseConfig.doOnConnected());
        assertThat(customizedConfig.connectionObserver())
                .isNotNull()
                .isNotSameAs(baseConfig.connectionObserver());
    }

    private static HttpClientConfig httpClientConfig(final ReactorClientHttpConnector connector) {
        return Assertions.assertThat(connector)
                .extracting("httpClient", InstanceOfAssertFactories.type(HttpClient.class))
                .extracting(HttpClient::configuration, InstanceOfAssertFactories.type(HttpClientConfig.class))
                .actual();
    }
}
