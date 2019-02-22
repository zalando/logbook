package org.zalando.logbook.servlet;

import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class SecureLogbookFilter extends AbstractLogbookFilter {

    public SecureLogbookFilter() {
        this(Logbook.create());
    }

    public SecureLogbookFilter(final Logbook logbook) {
        super(logbook);
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        chain.doFilter(request, response);

        if (isUnauthorizedOrForbidden(response)) {
            final ResponseWritingStage stage = logRequest(request, skipBody(request)).process(response);
            logResponse(request, response, stage);
        }
    }

    private boolean isUnauthorizedOrForbidden(final HttpServletResponse response) {
        final int status = response.getStatus();
        return status == 401 || status == 403;
    }

}
