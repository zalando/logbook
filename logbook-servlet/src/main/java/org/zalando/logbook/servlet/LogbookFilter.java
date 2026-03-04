package org.zalando.logbook.servlet;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.With;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;
import org.zalando.logbook.Strategy;

import java.io.IOException;
import java.util.UUID;

import static jakarta.servlet.DispatcherType.ASYNC;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@AllArgsConstructor(access = PRIVATE)
public final class LogbookFilter implements HttpFilter {

    /**
     * Unique per instance, so we don't accidentally share stages between filter
     * instances in the same chain.
     */
    private final String responseProcessingStageName = ResponseProcessingStage.class.getName() + "-" + UUID.randomUUID();

    private final Logbook logbook;
    @Nullable
    private final Strategy strategy;

    @With
    private final FormRequestMode formRequestMode;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, null);
    }

    public LogbookFilter(final Logbook logbook, @Nullable final Strategy strategy) {
        this(logbook, strategy, FormRequestMode.fromProperties());
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                         final FilterChain chain) throws ServletException, IOException {
        final RemoteRequest request = new RemoteRequest(httpRequest, formRequestMode);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        final ResponseProcessingStage processing;

        if (request.getDispatcherType() == ASYNC) {
            processing = (ResponseProcessingStage) request.getAttribute(responseProcessingStageName);
        } else {
            processing = process(request).write();
            request.setAttribute(responseProcessingStageName, processing);
        }

        final ResponseWritingStage writing = processing.process(response);

        chain.doFilter(request, response);

        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new LogbookAsyncListener(event -> write(response, writing)));

            return;
        }

        // The async writing is handled by the attached on-complete listener
        if (request.getDispatcherType() != ASYNC) {
            write(response, writing);
        }
    }

    private void write(LocalResponse response, ResponseWritingStage writing) throws IOException {
        try {
            response.flushBuffer();
        } catch (IOException e) {
            // ignore and try to log the response anyway
        }
        writing.write();
    }

    private RequestWritingStage process(
            final HttpRequest request) throws IOException {

        return strategy == null ?
                logbook.process(request) :
                logbook.process(request, strategy);
    }
}
