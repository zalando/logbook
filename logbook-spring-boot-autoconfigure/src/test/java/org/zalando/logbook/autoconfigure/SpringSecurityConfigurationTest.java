package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class SpringSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void registersSpringSecurityAttributeExtractorWhenNoCustomBeanPresent() {
        this.contextRunner
                .withUserConfiguration(LogbookAutoConfiguration.SpringSecurityConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AttributeExtractor.class);
                    assertThat(context.getBean(AttributeExtractor.class))
                            .isInstanceOf(SpringSecurityAttributeExtractor.class);
                });
    }

    @Test
    void doesNotOverrideCustomAttributeExtractor() {
        final AttributeExtractor custom = new AttributeExtractor() {
            @Override
            public HttpAttributes extract(final org.zalando.logbook.HttpRequest request) {
                return HttpAttributes.EMPTY;
            }
        };
        this.contextRunner
                .withBean(AttributeExtractor.class, () -> custom)
                .withUserConfiguration(LogbookAutoConfiguration.SpringSecurityConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AttributeExtractor.class);
                    assertThat(context.getBean(AttributeExtractor.class)).isSameAs(custom);
                });
    }

}
