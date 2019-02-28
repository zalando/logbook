package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.WithoutBodyStrategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@LogbookTest(properties = "logbook.strategy = without-body")
class WithoutBodyStrategyTest {

    @Autowired
    private Strategy strategy;

    @Test
    void shouldUseCorrectStrategy() {
        assertThat(strategy, is(instanceOf(WithoutBodyStrategy.class)));
    }

}
