package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.zalando.logbook.api.Logbook;

import static org.assertj.core.api.Assertions.assertThat;

class JavaxFilterTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner();

    @Test
    void shouldInitializeFilter() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withBean("logbookProperties", LogbookProperties.class, LogbookProperties::new)
                .withUserConfiguration(LogbookAutoConfiguration.JavaxServletFilterConfiguration.class)
                .withClassLoader(new FilteredClassLoader(org.zalando.logbook.servlet.LogbookFilter.class))
                .withClassLoader(new FilteredClassLoader(jakarta.servlet.Servlet.class))
                .withPropertyValues("logbook.filter.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("logbookFilter");
                });

    }

    @Test
    void shouldInitializeSecureFilter() {
        this.contextRunner
                .withBean("logbook", Logbook.class, Logbook::create)
                .withBean("logbookProperties", LogbookProperties.class, LogbookProperties::new)
                .withUserConfiguration(LogbookAutoConfiguration.JavaxSecurityServletFilterConfiguration.class)
                .withClassLoader(new FilteredClassLoader(org.zalando.logbook.servlet.LogbookFilter.class))
                .withClassLoader(new FilteredClassLoader(jakarta.servlet.Servlet.class))
                .withPropertyValues("logbook.secure-filter.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("secureLogbookFilter");
                });

    }

}
