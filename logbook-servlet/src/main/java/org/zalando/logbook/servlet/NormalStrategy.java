package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;
import org.zalando.logbook.RequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION_TYPE;
import static org.zalando.logbook.servlet.Attributes.CORRELATOR;

final class NormalStrategy implements Strategy {

    private final RequestFilter filter;

    NormalStrategy(final RequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public void doFilter(final Logbook logbook, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final ResponseProcessingStage stage = logRequest(logbook, request);

        final String protocolVersion = request.getProtocolVersion();
        final LocalResponse response = new LocalResponse(httpResponse, protocolVersion);

        final ResponseWritingStage writingStage = stage.process(response);

        chain.doFilter(request, response);
        logResponse(writingStage, request, response);
    }

    private ResponseProcessingStage logRequest(final Logbook logbook,
            final RemoteRequest request) throws IOException {
        if (isFirstRequest(request)) {
            final ResponseProcessingStage stage = logbook.process(skipBodyIfErrorDispatch(request)).write();
            request.setAttribute(CORRELATOR, stage);
            return stage;
        } else {
            return readCorrelator(request);
        }
    }

    private HttpRequest skipBodyIfErrorDispatch(final RemoteRequest request) {
        if (request.getAttribute(ERROR_EXCEPTION_TYPE) == null) {
            return request;
        }
        return filter.filter(request);
    }

    private ResponseProcessingStage readCorrelator(final RemoteRequest request) {
        return (ResponseProcessingStage) request.getAttribute(CORRELATOR);
    }

    private void logResponse(final ResponseWritingStage correlator, final RemoteRequest request,
            final LocalResponse response) throws IOException {

        if (isLastRequest(request)) {
            response.getWriter().flush();
            correlator.write();
        }
    }

}
