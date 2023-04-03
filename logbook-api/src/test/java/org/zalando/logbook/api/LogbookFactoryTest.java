package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class LogbookFactoryTest {

    @Test
    void shouldLoadInstanceUsingSPI() {
        final LogbookFactory factory = LogbookFactory.INSTANCE;

        assertThat(factory).isInstanceOf(MockbookFactory.class);
    }

}
