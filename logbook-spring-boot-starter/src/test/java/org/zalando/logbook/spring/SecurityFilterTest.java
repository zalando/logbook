package org.zalando.logbook.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@LogbookTest
class SecurityFilterTest {

    @Autowired
    @Qualifier("secureLogbookFilter")
    private FilterRegistrationBean secureLogbookFilter;

    @Autowired
    @Qualifier("logbookFilter")
    private FilterRegistrationBean logbookFilter;

    @Test
    void shouldInitializeFilters() {
        assertThat(secureLogbookFilter, is(notNullValue()));
        assertThat(logbookFilter, is(notNullValue()));
    }

}
