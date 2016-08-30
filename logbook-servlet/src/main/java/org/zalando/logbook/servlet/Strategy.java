package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Strategy {

    Strategy NORMAL = new NormalStrategy();
    Strategy SECURITY = new SecurityStrategy();

    void doFilter(final Logbook logbook, final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException;


    default boolean isFirstRequest(final HttpServletRequest request) {
        return request.getDispatcherType() != DispatcherType.ASYNC;
    }

    default boolean isLastRequest(final HttpServletRequest request) {
        return !request.isAsyncStarted();
    }


}

