package org.zalando.logbook.servlet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Verifies that {@link LogbookFilter} rejects non-HTTP requests/responses.
 */
public final class HttpSupportTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final Logbook logbook = mock(Logbook.class);
    private final LogbookFilter unit = new LogbookFilter(logbook);

    private final ServletRequest request = mock(HttpServletRequest.class);
    private final ServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void shouldRejectNonHttpRequest() throws ServletException, IOException {
        final ServletRequest nonHttpRequest = mock(ServletRequest.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("LogbookFilter only supports HTTP");

        unit.doFilter(nonHttpRequest, response, chain);
    }

    @Test
    public void shouldRejectNonHttpResponse() throws ServletException, IOException {
        final ServletResponse nonHttpResponse = mock(ServletResponse.class);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("LogbookFilter only supports HTTP");

        unit.doFilter(request, nonHttpResponse, chain);
    }

}