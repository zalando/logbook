package org.zalando.logbook.servlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class ForwardingOnceFilter extends OnceFilter {

    private final OnceFilter filter;

    ForwardingOnceFilter(final OnceFilter filter) {
        this.filter = filter;
    }

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        filter.doFilter(request, response, chain);
    }

}
