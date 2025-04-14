package org.zalando.logbook.autoconfigure.webflux;


import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxClientConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyClientConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration;
import org.zalando.logbook.autoconfigure.webflux.LogbookWebFluxAutoConfiguration.WebFluxServerConfiguration;
import org.zalando.logbook.netty.LogbookServerHandler;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void shouldAddLogbookHandlerLastToPipelineWhenNoHttpTrafficHandlerOrHttpCodecPresent() {
        ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
                .withUserConfiguration(LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration.class)
                .withBean(Logbook.class, Logbook::create);

        contextRunner.run(context -> {
            HttpServer server = mock(HttpServer.class);

            NettyServerCustomizer customizer = context.getBean(NettyServerCustomizer.class);
            assertThat(customizer).isNotNull();

            ChannelPipeline pipeline = mock(ChannelPipeline.class);
            mockDoOnConnection(server, pipeline);

            // Apply the customizer
            customizer.apply(server);

            // Verify that the LogbookServerHandler was added to the pipeline
            ArgumentCaptor<LogbookServerHandler> handlerCaptor = ArgumentCaptor.forClass(LogbookServerHandler.class);
            verify(pipeline).addLast(eq("logbookHandler"), handlerCaptor.capture());
            assertThat(handlerCaptor.getValue()).isNotNull();
        });
    }

    @Test
    public void shouldAddLogbookHandlerToPipelineAfterHttpTrafficHandler() {
        ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
                .withUserConfiguration(LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration.class)
                .withBean(Logbook.class, Logbook::create);

        contextRunner.run(context -> {
            HttpServer server = mock(HttpServer.class);

            NettyServerCustomizer customizer = context.getBean(NettyServerCustomizer.class);
            assertThat(customizer).isNotNull();

            ChannelPipeline pipeline = mock(ChannelPipeline.class);
            when(pipeline.get(NettyPipeline.HttpTrafficHandler)).thenReturn(mock());
            mockDoOnConnection(server, pipeline);

            // Apply the customizer
            customizer.apply(server);

            // Verify that the LogbookServerHandler was added to the pipeline
            ArgumentCaptor<LogbookServerHandler> handlerCaptor = ArgumentCaptor.forClass(LogbookServerHandler.class);
            verify(pipeline).addAfter(eq(NettyPipeline.HttpTrafficHandler), eq("logbookHandler"), handlerCaptor.capture());
            assertThat(handlerCaptor.getValue()).isNotNull();
        });
    }

    @Test
    public void shouldAddLogbookHandlerToPipelineAfterHttpCodecWhenNoHttpTrafficHandlerPresent() {
        ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
                .withUserConfiguration(LogbookWebFluxAutoConfiguration.WebFluxNettyServerConfiguration.class)
                .withBean(Logbook.class, Logbook::create);

        contextRunner.run(context -> {
            HttpServer server = mock(HttpServer.class);

            NettyServerCustomizer customizer = context.getBean(NettyServerCustomizer.class);
            assertThat(customizer).isNotNull();

            ChannelPipeline pipeline = mock(ChannelPipeline.class);
            when(pipeline.get(NettyPipeline.HttpCodec)).thenReturn(mock());
            mockDoOnConnection(server, pipeline);

            // Apply the customizer
            customizer.apply(server);

            // Verify that the LogbookServerHandler was added to the pipeline
            ArgumentCaptor<LogbookServerHandler> handlerCaptor = ArgumentCaptor.forClass(LogbookServerHandler.class);
            verify(pipeline).addAfter(eq(NettyPipeline.HttpCodec), eq("logbookHandler"), handlerCaptor.capture());
            assertThat(handlerCaptor.getValue()).isNotNull();
        });
    }

    @SuppressWarnings("unchecked")
    private static void mockDoOnConnection(HttpServer server, ChannelPipeline pipeline) {
        when(server.doOnConnection(any())).thenAnswer(invocation -> {
            reactor.netty.Connection connection = mockConnection(pipeline);
            ((java.util.function.Consumer<reactor.netty.Connection>) invocation.getArgument(0)).accept(connection);
            return server;
        });
    }

    private static reactor.netty.Connection mockConnection(ChannelPipeline pipeline) {
        reactor.netty.Connection connection = mock(reactor.netty.Connection.class);
        Channel channel = mock(Channel.class);
        when(connection.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        return connection;
    }

    private ReactiveWebApplicationContextRunner initContextRunner() {
        return new ReactiveWebApplicationContextRunner()
                .withUserConfiguration(LogbookWebFluxAutoConfiguration.class)
                .withBean(Logbook.class, Logbook::create);
    }
}
