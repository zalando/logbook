package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.api.Strategy;
import org.zalando.logbook.core.WithoutBodyStrategy;

import static org.assertj.core.api.Assertions.assertThat;

@LogbookTest(properties = "logbook.strategy = without-body")
class WithoutBodyStrategyTest {

    @Autowired
    private Strategy strategy;

    @Test
    void shouldUseCorrectStrategy() {
        assertThat(strategy).isInstanceOf(WithoutBodyStrategy.class);
    }

}
