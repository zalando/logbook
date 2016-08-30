package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class LogbookFactoryTest {

    @Test
    public void shouldLoadInstanceUsingSPI() {
        final LogbookFactory factory = LogbookFactory.INSTANCE;

        assertThat(factory, is(instanceOf(MockbookFactory.class)));
    }

}