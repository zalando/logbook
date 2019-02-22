package org.zalando.logbook.servlet;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class SecurityFilter implements Filter {

    @Nullable
    private Integer status;

    void setStatus(final Integer status) {
        this.status = status;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        // nothing to do
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        if (status == null) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(status);
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }

}
