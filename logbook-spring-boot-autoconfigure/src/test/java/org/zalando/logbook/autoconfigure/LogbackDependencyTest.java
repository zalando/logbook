package org.zalando.logbook.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ContextBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogbackDependencyTest {

    @Test
    void shouldAlignCoreAndClassicVersions() {
        assertThat(LoggerContext.class.getPackage().getImplementationVersion())
                .isNotBlank()
                .isEqualTo(ContextBase.class.getPackage().getImplementationVersion());
    }

}
