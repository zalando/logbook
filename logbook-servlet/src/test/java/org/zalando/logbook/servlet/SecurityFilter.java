package org.zalando.logbook.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.annotation.Nullable;
import java.io.IOException;

class SecurityFilter implements HttpFilter {

    @Nullable
    private Integer status;

    void setStatus(final Integer status) {
        this.status = status;
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        if (status == null) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(status);
        }
    }

}
