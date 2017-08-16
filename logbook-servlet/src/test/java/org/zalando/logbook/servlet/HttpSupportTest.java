package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Verifies that {@link LogbookFilter} rejects non-HTTP requests/responses.
 */
public final class HttpSupportTest {

    private final Logbook logbook = mock(Logbook.class);
    private final LogbookFilter unit = new LogbookFilter(logbook);

    private final ServletRequest request = mock(HttpServletRequest.class);
    private final ServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    void shouldRejectNonHttpRequest() throws ServletException, IOException {
        final ServletRequest nonHttpRequest = mock(ServletRequest.class);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                unit.doFilter(nonHttpRequest, response, chain));

        assertThat(exception.getMessage(), is("LogbookFilter only supports HTTP"));
    }

    @Test
    void shouldRejectNonHttpResponse() throws ServletException, IOException {
        final ServletResponse nonHttpResponse = mock(ServletResponse.class);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                unit.doFilter(request, nonHttpResponse, chain));

        assertThat(exception.getMessage(), is("LogbookFilter only supports HTTP"));
    }

}
