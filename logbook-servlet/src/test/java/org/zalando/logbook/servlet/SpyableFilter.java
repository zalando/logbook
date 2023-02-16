package org.zalando.logbook.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

// non-final so we can wrap a spy around it
class SpyableFilter implements Filter {

    private final Filter filter;

    SpyableFilter(final Filter filter) {
        this.filter = filter;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        filter.init(filterConfig);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        filter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        filter.destroy();
    }

}
