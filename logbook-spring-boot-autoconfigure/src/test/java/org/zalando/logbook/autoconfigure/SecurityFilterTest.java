package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.Logbook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityFilterTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(LogbookAutoConfiguration.JakartaSecurityServletFilterConfiguration.class);

    @Test
    void shouldRegisterSecureLogbookFilterWhenSecurityFilterChainIsPresent() {
        contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withBean("securityFilterChain", SecurityFilterChain.class, () -> mock(SecurityFilterChain.class))
                .run(context -> assertThat(context).hasBean("secureLogbookFilter"));
    }

    @Test
    void shouldNotRegisterSecureLogbookFilterWithoutSecurityFilterChain() {
        contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .run(context -> assertThat(context).doesNotHaveBean("secureLogbookFilter"));
    }

}
