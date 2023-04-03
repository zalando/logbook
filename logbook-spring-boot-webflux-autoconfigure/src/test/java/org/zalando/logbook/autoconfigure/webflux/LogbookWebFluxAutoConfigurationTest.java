package org.zalando.logbook.autoconfigure.webflux;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxClientConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyClientConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxServerConfiguration;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

public class LogbookWebFluxAutoConfigurationTest {

    @Test
    public void shouldInitializeNettyServerCustomizer() {
        initContextRunner()
                .run(context -> assertThat(context).hasBean(WebFluxNettyServerConfiguration.CUSTOMIZER_NAME));
    }

    @Test
    public void shouldNotInitializeNettyServerCustomizer() {
        initContextRunner()
                .withPropertyValues("logbook.filter.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WebFluxNettyServerConfiguration.CUSTOMIZER_NAME));
    }

    @Test
    public void shouldInitializeNettyClientCustomizer() {
        initContextRunner()
                .run(context -> assertThat(context).hasBean(WebFluxNettyClientConfiguration.CUSTOMIZER_NAME));
    }

    @Test
    public void shouldInitializeWebFilter() {
        initContextRunner()
                .withClassLoader(new FilteredClassLoader(HttpServer.class))
                .run(context -> assertThat(context).hasBean(WebFluxServerConfiguration.CUSTOMIZER_NAME));
    }

    @Test
    public void shouldInitializeExchangeFilterFunction() {
        initContextRunner()
                .withClassLoader(new FilteredClassLoader(HttpClient.class))
                .run(context -> assertThat(context).hasBean(WebFluxClientConfiguration.CUSTOMIZER_NAME));
    }

    private ReactiveWebApplicationContextRunner initContextRunner() {
        return new ReactiveWebApplicationContextRunner()
                .withUserConfiguration(LogbookWebFluxAutoConfiguration.class)
                .withBean(Logbook.class, Logbook::create);
    }
}
