package org.zalando.logbook.servlet;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class LogbookFilter implements HttpFilter {

    private static final String STAGE = ResponseProcessingStage.class.getName();

    private final Logbook logbook;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        final ResponseWritingStage stage = logRequest(request, request).process(response);

        chain.doFilter(request, response);

        if (request.isAsyncStarted()) {
            return;
        }

        response.getWriter().flush();
        stage.write();
    }

    private ResponseProcessingStage logRequest(final HttpServletRequest httpRequest,
            final HttpRequest request) throws IOException {

        if (httpRequest.getDispatcherType() == DispatcherType.ASYNC) {
            return (ResponseProcessingStage) httpRequest.getAttribute(STAGE);
        } else {
            final ResponseProcessingStage stage = logbook.process(request).write();
            httpRequest.setAttribute(STAGE, stage);
            return stage;
        }
    }

}
