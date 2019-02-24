package org.zalando.logbook.servlet;

import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class SecureLogbookFilter implements HttpFilter {

    private static final String STAGE = RequestWritingStage.class.getName();

    private final Logbook logbook;

    public SecureLogbookFilter() {
        this(Logbook.create());
    }

    public SecureLogbookFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        final RequestWritingStage writeRequest = processRequest(httpRequest, request);

        // effectively overriding the decision from the underlying strategy
        request.withoutBody();

        chain.doFilter(request, response);

        if (request.isAsyncStarted()) {
            return;
        }

        if (isUnauthorizedOrForbidden(response)) {
            final ResponseWritingStage process = writeRequest.write().process(response);
            response.getWriter().flush();
            process.write();
        }
    }

    private RequestWritingStage processRequest(final HttpServletRequest httpRequest, final RemoteRequest request) throws IOException {
        if (httpRequest.getDispatcherType() == DispatcherType.ASYNC) {
            return (RequestWritingStage) httpRequest.getAttribute(STAGE);
        } else {
            final RequestWritingStage stage = logbook.process(request);
            httpRequest.setAttribute(STAGE, stage);
            return stage;
        }
    }

    private boolean isUnauthorizedOrForbidden(final HttpServletResponse response) {
        final int status = response.getStatus();
        return status == 401 || status == 403;
    }

}
