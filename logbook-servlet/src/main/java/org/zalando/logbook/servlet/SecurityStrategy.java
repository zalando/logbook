package org.zalando.logbook.servlet;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.RequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.zalando.logbook.servlet.Attributes.CORRELATOR;

final class SecurityStrategy implements Strategy {

    private final RequestFilter filter;

    SecurityStrategy(final RequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public void doFilter(final Logbook logbook, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final String protocolVersion = request.getProtocolVersion();
        final LocalResponse response = new LocalResponse(httpResponse, protocolVersion);

        chain.doFilter(request, response);

        if (isUnauthorizedOrForbidden(response)) {
            final ResponseProcessingStage correlator;

            if (isFirstRequest(request)) {
                correlator = logbook.process(filter.filter(request)).write();
            } else {
                correlator = readCorrelator(request);
            }

            correlator.process(response).write();
        }
    }

    private boolean isUnauthorizedOrForbidden(final HttpServletResponse response) {
        final int status = response.getStatus();
        return status == 401 || status == 403;
    }

    private ResponseProcessingStage readCorrelator(final RemoteRequest request) {
        return (ResponseProcessingStage) request.getAttribute(CORRELATOR);
    }

}
