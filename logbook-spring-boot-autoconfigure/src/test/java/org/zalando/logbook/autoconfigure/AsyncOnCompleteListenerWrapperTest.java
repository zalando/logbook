package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.servlet.AsyncOnCompleteListener;
import org.zalando.logbook.servlet.AsyncOnCompleteListenerWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AsyncOnCompleteListenerWrapperTest {

    private static final String BEAN_NAME = "asyncOnCompleteListenerWrapper";

    private static final AsyncOnCompleteListenerWrapper CUSTOM = listener -> listener;

    private final WebApplicationContextRunner servlet = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class));

    @Test
    void shouldRegisterIdentityWrapperByDefault() {
        servlet.run(context -> {
            assertThat(context).hasSingleBean(AsyncOnCompleteListenerWrapper.class).hasBean(BEAN_NAME);

            final AsyncOnCompleteListener listener = mock(AsyncOnCompleteListener.class);
            assertThat(context.getBean(AsyncOnCompleteListenerWrapper.class).wrap(listener)).isSameAs(listener);
        });
    }

    @Test
    void shouldBackOffInFavourOfUserDefinedWrapper() {
        servlet.withBean(BEAN_NAME, AsyncOnCompleteListenerWrapper.class, () -> CUSTOM).run(context ->
                assertThat(context)
                        .hasSingleBean(AsyncOnCompleteListenerWrapper.class)
                        .getBean(AsyncOnCompleteListenerWrapper.class)
                        .isSameAs(CUSTOM));
    }

    @Test
    void shouldBackOffInFavourOfUserDefinedWrapperWithDifferentBeanName() {
        servlet.withUserConfiguration(DifferentlyNamedWrapperConfiguration.class).run(context ->
                assertThat(context)
                        .hasSingleBean(AsyncOnCompleteListenerWrapper.class)
                        .hasBean("myTracingWrapper")
                        .doesNotHaveBean(BEAN_NAME));
    }

    @Test
    void shouldNotRegisterWrapperForNonServletApplications() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(AsyncOnCompleteListenerWrapper.class));

        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LogbookAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(AsyncOnCompleteListenerWrapper.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class DifferentlyNamedWrapperConfiguration {

        @Bean
        public AsyncOnCompleteListenerWrapper myTracingWrapper() {
            return CUSTOM;
        }
    }

}
