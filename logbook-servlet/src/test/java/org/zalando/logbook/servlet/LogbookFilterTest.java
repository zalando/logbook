package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterConfig;

import static org.mockito.Mockito.mock;

final class LogbookFilterTest {

    @Test
    void shouldCreateLogbookFilter() {
        new LogbookFilter();
    }

    @Test
    void shouldCreateSecureLogbookFilter() {
        new SecureLogbookFilter();
    }

    @Test
    void shouldCallInit() {
        new LogbookFilter().init(mock(FilterConfig.class));
    }

    @Test
    void shouldCallDestroy() {
        new LogbookFilter().destroy();
    }

}
