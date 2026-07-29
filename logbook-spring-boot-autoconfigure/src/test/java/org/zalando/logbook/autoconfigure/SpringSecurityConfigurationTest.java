package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class SpringSecurityConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LogbookAutoConfiguration.SpringSecurityConfiguration.class);

    @Test
    void shouldNotRegisterSpringSecurityAttributeExtractorByDefault() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AttributeExtractor.class);
                });
    }

    @Test
    void shouldNotRegisterSpringSecurityAttributeExtractorWhenDisabled() {
        this.contextRunner
                .withPropertyValues("logbook.security.attribute-extractor.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AttributeExtractor.class);
                });
    }

    @Test
    void registersSpringSecurityAttributeExtractorWhenEnabled() {
        this.contextRunner
                .withPropertyValues("logbook.security.attribute-extractor.enabled=true")
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
                .withPropertyValues("logbook.security.attribute-extractor.enabled=true")
                .withBean(AttributeExtractor.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasSingleBean(AttributeExtractor.class);
                    assertThat(context.getBean(AttributeExtractor.class)).isSameAs(custom);
                });
    }

}
