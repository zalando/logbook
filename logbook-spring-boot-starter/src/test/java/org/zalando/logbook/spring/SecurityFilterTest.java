package org.zalando.logbook.spring;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
public class SecurityFilterTest extends AbstractTest {

    @Autowired
    @Qualifier("unauthorizedLogbookFilter")
    private FilterRegistrationBean unauthorizedLogbookFilter;

    @Autowired
    @Qualifier("authorizedLogbookFilter")
    private FilterRegistrationBean authorizedLogbookFilter;

    @Test
    public void shouldInitializeFilters() {
        assertThat(unauthorizedLogbookFilter, is(notNullValue()));
        assertThat(authorizedLogbookFilter, is(notNullValue()));
    }

}