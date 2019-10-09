package org.zalando.logbook.servlet;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;
import org.zalando.logbook.Strategy;

import javax.annotation.Nullable;
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
    private final Strategy strategy;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, null);
    }

    public LogbookFilter(final Logbook logbook, @Nullable final Strategy strategy) {
        this.logbook = logbook;
        this.strategy = strategy;
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
            final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        final ResponseWritingStage stage = logRequest(request).process(response);

        chain.doFilter(request, response);

        if (request.isAsyncStarted()) {
            return;
        }

        response.flushBuffer();
        stage.write();
    }

    private ResponseProcessingStage logRequest(final RemoteRequest request) throws IOException {
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return (ResponseProcessingStage) request.getAttribute(STAGE);
        } else {
            final ResponseProcessingStage stage = process(request).write();
            request.setAttribute(STAGE, stage);
            return stage;
        }
    }

    private RequestWritingStage process(final HttpRequest request) throws IOException {
        return strategy == null ?
                logbook.process(request) :
                logbook.process(request, strategy);
    }

}
