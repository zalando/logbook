package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Logbook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class StandardTest extends AbstractTest {

    @Autowired
    private Logbook logbook;

    @Test
    void shouldBeAutowired() {
        assertThat(logbook, is(notNullValue()));
    }

}
