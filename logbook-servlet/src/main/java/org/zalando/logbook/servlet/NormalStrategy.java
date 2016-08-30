package org.zalando.logbook.servlet;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.zalando.logbook.servlet.Attributes.CORRELATOR;

final class NormalStrategy implements Strategy {

    @Override
    public void doFilter(final Logbook logbook, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final Optional<Correlator> correlator = logRequestIfNecessary(logbook, request);

        if (correlator.isPresent()) {
            final String protocolVersion = request.getProtocolVersion();
            final LocalResponse response = new LocalResponse(httpResponse, protocolVersion);

            chain.doFilter(request, response);
            response.getWriter().flush();
            logResponse(correlator.get(), request, response);
        } else {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private Optional<Correlator> logRequestIfNecessary(final Logbook logbook, final RemoteRequest request) throws IOException {
        if (isFirstRequest(request)) {
            final Optional<Correlator> correlator = logbook.write(request);
            correlator.ifPresent(writeCorrelator(request));
            return correlator;
        } else {
            return readCorrelator(request);
        }
    }

    private Consumer<Correlator> writeCorrelator(final RemoteRequest request) {
        return correlator -> request.setAttribute(CORRELATOR, correlator);
    }

    private Optional<Correlator> readCorrelator(final RemoteRequest request) {
        return Optional.ofNullable(request.getAttribute(CORRELATOR)).map(Correlator.class::cast);
    }

    private void logResponse(final Correlator correlator, final RemoteRequest request,
            final LocalResponse response) throws IOException {

        if (isLastRequest(request)) {
            correlator.write(response);
        }
    }

}
