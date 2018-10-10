package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@LogbookTest(properties = "logbook.filter.enabled = false")
class FilterDisabledTest {

    @Autowired(required = false)
    @Qualifier("authorizedLogbookFilter")
    private FilterRegistrationBean authorizedLogbookFilter;

    @Test
    void shouldInitializeFilter() {
        assertThat(authorizedLogbookFilter, is(nullValue()));
    }

}
