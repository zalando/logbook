package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestClientTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void shouldRegisterRestClientCustomizer() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withUserConfiguration(
                        LogbookAutoConfiguration.ClientHttpAutoConfiguration.class,
                        LogbookAutoConfiguration.RestClientAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(LogbookClientHttpRequestInterceptor.class);
                    assertThat(context).hasBean("logbookRestClientCustomizer");
                    assertThat(context).hasSingleBean(RestClientCustomizer.class);
                });
    }

    @Test
    void shouldApplyInterceptorToRestClientBuilder() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withUserConfiguration(
                        LogbookAutoConfiguration.ClientHttpAutoConfiguration.class,
                        LogbookAutoConfiguration.RestClientAutoConfiguration.class)
                .run(context -> {
                    final LogbookClientHttpRequestInterceptor interceptor =
                            context.getBean(LogbookClientHttpRequestInterceptor.class);
                    final RestClientCustomizer customizer = context.getBean(
                            "logbookRestClientCustomizer", RestClientCustomizer.class);

                    final RestClient.Builder builder = mock(RestClient.Builder.class);
                    when(builder.requestInterceptor(same(interceptor))).thenReturn(builder);

                    customizer.customize(builder);

                    verify(builder).requestInterceptor(same(interceptor));
                });
    }

    @Test
    void shouldBackOffWhenCustomRestClientCustomizerBeanNamePresent() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withBean("logbookRestClientCustomizer", RestClientCustomizer.class,
                        () -> builder -> {
                        })
                .withBean(LogbookClientHttpRequestInterceptor.class,
                        () -> new LogbookClientHttpRequestInterceptor(Logbook.create()))
                .withUserConfiguration(LogbookAutoConfiguration.RestClientAutoConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("logbookRestClientCustomizer");
                    assertThat(context.getBeansOfType(RestClientCustomizer.class)).hasSize(1);
                });
    }

}
