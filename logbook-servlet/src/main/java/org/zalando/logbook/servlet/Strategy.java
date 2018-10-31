package org.zalando.logbook.servlet;

import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.zalando.logbook.RequestFilters.replaceBody;

@API(status = MAINTAINED)
public interface Strategy {

    Strategy NORMAL = new NormalStrategy(replaceBody(message -> "<skipped>"));
    Strategy SECURITY = new SecurityStrategy(replaceBody(message -> "<skipped>"));

    void doFilter(Logbook logbook, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            FilterChain chain) throws ServletException, IOException;

    default boolean isFirstRequest(final HttpServletRequest request) {
        return request.getDispatcherType() != DispatcherType.ASYNC;
    }

    default boolean isLastRequest(final HttpServletRequest request) {
        return !request.isAsyncStarted();
    }


}

