package org.zalando.logbook.spring;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.zalando.logbook.Logbook;

import javax.annotation.Nullable;
import java.io.IOException;

@API(status = API.Status.EXPERIMENTAL)
@AllArgsConstructor
@Slf4j
public final class LogbookClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Logbook logbook;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        final Logbook.ResponseProcessingStage stage = logRequest(request, body);
        final ClientHttpResponse response = new BufferingClientHttpResponseWrapper(execution.execute(request, body));
        logResponse(stage, response);

        return response;
    }

    @Nullable
    private Logbook.ResponseProcessingStage logRequest(HttpRequest request, byte[] body) {
        Logbook.ResponseProcessingStage stage = null;
        try {
            final org.zalando.logbook.HttpRequest httpRequest = new LocalRequest(request, body);
            stage = logbook.process(httpRequest).write();
        } catch (Exception e) {
            log.trace("Unable to log request: {}", e.getClass());
        }
        return stage;
    }

    private static void logResponse(@Nullable final Logbook.ResponseProcessingStage stage, final ClientHttpResponse response) {
        if (stage != null) {
            try {
                final RemoteResponse httpResponse = new RemoteResponse(response);
                stage.process(httpResponse).write();
            } catch (Exception e) {
                log.trace("Unable to log response: {}", e.getClass());
            }
        } else {
            log.trace("Unable to log response: ResponseProcessingStage is null");
        }
    }
}
