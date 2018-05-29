package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import static org.mockito.Mockito.mock;

public final class LogbookFilterTest {

    @Test
    void shouldCreateLogbookFilter() {
        new LogbookFilter();
    }

    @Test
    void shouldCallInit() throws ServletException {
        new LogbookFilter().init(mock(FilterConfig.class));
    }

    @Test
    void shouldCallDestroy() {
        new LogbookFilter().destroy();
    }

}
