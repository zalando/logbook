package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.Strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LogbookTest(properties = "logbook.strategy = body-only-if-status-at-least")
class BodyOnlyIfStatusAtLeastStrategyTest {

    @Autowired
    private Strategy strategy;

    @Test
    void shouldUseCorrectStrategy() {
        assertThat(strategy, is(instanceOf(BodyOnlyIfStatusAtLeastStrategy.class)));
    }

}
