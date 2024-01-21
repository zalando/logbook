package org.zalando.logbook.httpclient5;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.zalando.logbook.Logbook;

import javax.annotation.Nullable;
import java.io.IOException;

@Slf4j
public class LogbookHttpExecHandler implements ExecChainHandler {

    private final Logbook logbook;

    public LogbookHttpExecHandler(Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public ClassicHttpResponse execute(final ClassicHttpRequest request, final ExecChain.Scope scope, final ExecChain execChain) throws IOException, HttpException {
        Logbook.ResponseProcessingStage stage = logRequest(request);
        final ClassicHttpResponse response = execChain.proceed(request, scope);
        logResponse(stage, response);

        return response;
    }

    @Nullable
    private Logbook.ResponseProcessingStage logRequest(final ClassicHttpRequest request) {
        Logbook.ResponseProcessingStage stage = null;
        try {
            final LocalRequest localRequest = new LocalRequest(request, request.getEntity());
            stage = logbook.process(localRequest).write();
        } catch (Exception e) {
            log.trace("Unable to log request: {}", e.getClass());
        }
        return stage;
    }

    private static void logResponse(@Nullable final Logbook.ResponseProcessingStage stage, final ClassicHttpResponse response) {
        if (stage != null) {
            try {
                stage.process(new RemoteResponse(response)).write();
            } catch (Exception e) {
                log.trace("Unable to log response: {}", e.getClass());
            }
        } else {
            log.trace("Unable to log response: ResponseProcessingStage is null");
        }
    }
}
