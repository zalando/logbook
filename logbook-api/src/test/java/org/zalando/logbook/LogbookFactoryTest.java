package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public final class LogbookFactoryTest {

    @Test
    void shouldLoadInstanceUsingSPI() {
        final LogbookFactory factory = LogbookFactory.INSTANCE;

        assertThat(factory, is(instanceOf(MockbookFactory.class)));
    }

}
