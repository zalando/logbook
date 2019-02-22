package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.RequestFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.zalando.logbook.RequestFilters.replaceBody;

abstract class AbstractLogbookFilter implements Filter {

    private static final String STAGE = ResponseProcessingStage.class.getName();

    private final RequestFilter filter = replaceBody(message -> "<skipped>");
    private final Logbook logbook;

    protected AbstractLogbookFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public final void init(final FilterConfig filterConfig) {
        // no initialization needed by default
    }

    @Override
    public final void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " only supports HTTP");
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        doFilter(httpRequest, httpResponse, chain);
    }

    protected abstract void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException;

    protected final ResponseProcessingStage logRequest(final HttpServletRequest httpRequest,
            final HttpRequest request) throws IOException {

        if (isFirstRequest(httpRequest)) {
            final ResponseProcessingStage stage = logbook.process(request).write();
            httpRequest.setAttribute(STAGE, stage);
            return stage;
        } else {
            return (ResponseProcessingStage) httpRequest.getAttribute(STAGE);
        }
    }

    protected final void logResponse(final RemoteRequest request, final LocalResponse response,
            final Logbook.ResponseWritingStage stage) throws IOException {

        if (isLastRequest(request)) {
            response.getWriter().flush();
            stage.write();
        }
    }

    protected final boolean isFirstRequest(final HttpServletRequest request) {
        return request.getDispatcherType() != DispatcherType.ASYNC;
    }

    protected final boolean isLastRequest(final HttpServletRequest request) {
        return !request.isAsyncStarted();
    }

    protected final HttpRequest skipBody(final RemoteRequest request) {
        return filter.filter(request);
    }

    @Override
    public final void destroy() {
        // no deconstruction needed by default
    }

}
