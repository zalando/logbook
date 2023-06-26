package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.zalando.logbook.Logbook;

import static org.assertj.core.api.Assertions.assertThat;

class FeignTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner();

    @Test
    void shouldInitializeFeignLogbookLogger() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withBean("logbookProperties", LogbookProperties.class, LogbookProperties::new)
                .withUserConfiguration(LogbookAutoConfiguration.FeignLogbookLoggerConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("feignLogbookLogger");
                });
    }

}
