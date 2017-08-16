package org.zalando.logbook.servlet;

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class SecurityFilter implements HttpFilter {

    @Nullable
    private Integer status;

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain)
            throws ServletException, IOException {

        if (status == null) {
            chain.doFilter(httpRequest, httpResponse);
        } else {
            httpResponse.setStatus(status);
        }
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }
}
