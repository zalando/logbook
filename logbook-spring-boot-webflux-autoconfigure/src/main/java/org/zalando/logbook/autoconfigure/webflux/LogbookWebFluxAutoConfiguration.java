package org.zalando.logbook.autoconfigure.webflux;

import org.apiguardian.api.API;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.http.client.autoconfigure.reactive.ClientHttpConnectorBuilderCustomizer;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.springframework.boot.reactor.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.WebFilter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.netty.LogbookServerHandler;
import org.zalando.logbook.spring.webflux.LogbookExchangeFilterFunction;
import org.zalando.logbook.spring.webflux.LogbookWebFilter;
import reactor.netty.http.server.HttpServer;

import java.net.http.HttpClient;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;

@API(status = EXPERIMENTAL)
@Configuration(proxyBeanMethods = false)
public class LogbookWebFluxAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HttpServer.class)
    @ConditionalOnWebApplication(type = REACTIVE)
    static class WebFluxNettyServerConfiguration {

        static final String CUSTOMIZER_NAME = "logbookNettyServerCustomizer";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
        public NettyServerCustomizer logbookNettyServerCustomizer(final Logbook logbook) {
            return httpServer -> httpServer.doOnConnection(connection -> connection.addHandlerLast(new LogbookServerHandler(logbook)));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass("reactor.netty.http.server.HttpServer")
    @ConditionalOnWebApplication(type = REACTIVE)
    static class WebFluxServerConfiguration {

        static final String CUSTOMIZER_NAME = "logbookServerFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
        public WebFilter logbookServerFilter(final Logbook logbook) {
            return new LogbookWebFilter(logbook);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HttpClient.class)
    static class WebFluxNettyClientConfiguration {

        static final String CUSTOMIZER_NAME = "logbookNettyClientCustomizer";

        @Bean
        @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
        public ClientHttpConnectorBuilderCustomizer<ReactorClientHttpConnectorBuilder> logbookNettyClientCustomizer(final Logbook logbook) {
            return new LogbookNettyClientCustomizer(logbook);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingClass("reactor.netty.http.client.HttpClient")
    static class WebFluxClientConfiguration {

        static final String CUSTOMIZER_NAME = "logbookClientExchangeFunction";

        @Bean
        @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
        public ExchangeFilterFunction logbookClientExchangeFunction(final Logbook logbook) {
            return new LogbookExchangeFilterFunction(logbook);
        }
    }
}
