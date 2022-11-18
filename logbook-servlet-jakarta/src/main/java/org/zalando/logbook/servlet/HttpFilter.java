package org.zalando.logbook.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

interface HttpFilter extends Filter {

    @Override
    default void init(final FilterConfig filterConfig) {
        // no initialization needed by default
    }

    @Override
    default void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " only supports HTTP");
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        doFilter(httpRequest, httpResponse, chain);
    }

    void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException;

    @Override
    default void destroy() {
        // no deconstruction needed by default
    }

}
