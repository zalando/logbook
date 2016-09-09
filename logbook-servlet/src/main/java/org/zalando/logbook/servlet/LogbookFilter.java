package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class LogbookFilter implements HttpFilter {

    private final Logbook logbook;
    private final Strategy strategy;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, Strategy.NORMAL);
    }

    public LogbookFilter(final Logbook logbook, final Strategy strategy) {
        this.logbook = logbook;
        this.strategy = strategy;
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        strategy.doFilter(logbook, httpRequest, httpResponse, chain);
    }

}
