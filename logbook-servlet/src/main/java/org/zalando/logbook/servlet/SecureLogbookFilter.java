package org.zalando.logbook.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apiguardian.api.API;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.core.SecurityStrategy;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class SecureLogbookFilter implements HttpFilter {

    private final HttpFilter filter;

    public SecureLogbookFilter() {
        this(Logbook.create());
    }

    public SecureLogbookFilter(final Logbook logbook) {
        this.filter = new LogbookFilter(logbook, new SecurityStrategy());
    }

    @Override
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        filter.doFilter(request, response, chain);
    }

}
