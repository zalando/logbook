package org.zalando.logbook.servlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// non final so we can wrap a spy around it
class SpyableFilter implements HttpFilter {

    private final HttpFilter filter;

    SpyableFilter(final HttpFilter filter) {
        this.filter = filter;
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        filter.doFilter(request, response, chain);
    }

}
