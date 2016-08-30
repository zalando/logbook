package org.zalando.logbook.spring;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Logbook;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public final class StandardTest extends AbstractTest {

    @Autowired
    private Logbook logbook;

    @Test
    public void shouldBeAutowired() {
        assertThat(logbook, is(notNullValue()));
    }

}