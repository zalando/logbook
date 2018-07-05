package org.zalando.logbook.servlet;

import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.RawRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.zalando.fauxpas.FauxPas.throwingConsumer;
import static org.zalando.logbook.servlet.Attributes.CORRELATOR;

final class SecurityStrategy implements Strategy {

    private final RawRequestFilter filter;

    SecurityStrategy(final RawRequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public void doFilter(final Logbook logbook, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse, final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final String protocolVersion = request.getProtocolVersion();
        final LocalResponse response = new LocalResponse(httpResponse, protocolVersion);

        chain.doFilter(request, response);

        if (isUnauthorizedOrForbibben(response)) {
            final Optional<Correlator> correlator;

            if (isFirstRequest(request)) {
                correlator = logbook.write(filter.filter(request));
            } else {
                correlator = readCorrelator(request);
            }

            correlator.ifPresent(throwingConsumer(c -> c.write(response)));
        }
    }

    private boolean isUnauthorizedOrForbibben(final HttpServletResponse response) {
        final int status = response.getStatus();
        return status == 401 || status == 403;
    }

    private Optional<Correlator> readCorrelator(final RemoteRequest request) {
        return Optional.ofNullable(request.getAttribute(CORRELATOR)).map(Correlator.class::cast);
    }

}
