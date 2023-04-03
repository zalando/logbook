package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.core.StatusAtLeastStrategy;
import org.zalando.logbook.api.Strategy;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.strategy = status-at-least")
class StatusAtLeastStrategyTest {

    @Autowired
    private Strategy strategy;

    @Test
    void shouldUseCorrectStrategy() {
        assertThat(strategy).isInstanceOf(StatusAtLeastStrategy.class);
    }

}
