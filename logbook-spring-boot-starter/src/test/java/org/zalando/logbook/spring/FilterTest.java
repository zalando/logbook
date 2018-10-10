package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@LogbookTest
class FilterTest {

    @Autowired
    @Qualifier("authorizedLogbookFilter")
    private FilterRegistrationBean authorizedLogbookFilter;

    @Test
    void shouldInitializeFilter() {
        assertThat(authorizedLogbookFilter, is(notNullValue()));
    }

}
