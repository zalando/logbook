package org.zalando.logbook.spring;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@WebAppConfiguration
@TestPropertySource(properties = "logbook.filter.enabled = false")
public final class FilterDisabledTest extends AbstractTest {

    @Autowired(required = false)
    @Qualifier("authorizedLogbookFilter")
    private FilterRegistrationBean authorizedLogbookFilter;

    @Test
    public void shouldInitializeFilter() {
        assertThat(authorizedLogbookFilter, is(nullValue()));
    }

}