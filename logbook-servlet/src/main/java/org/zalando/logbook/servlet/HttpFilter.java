package org.zalando.logbook.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


interface HttpFilter extends Filter {

    @Override
    default void init(final FilterConfig filterConfig) throws ServletException {

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

    }

}
