package org.zalando.logbook.okhttp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import jakarta.annotation.Nonnull;
import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
@Slf4j
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

    @Nonnull
    @Override
    public Response intercept(final Chain chain) throws IOException {
        final LocalRequest request = new LocalRequest(chain.request());
        final ResponseProcessingStage stage = logRequest(request);
        final RemoteResponse response = new RemoteResponse(chain.proceed(request.toRequest()));
        logResponse(stage, response);

        return response.toResponse();
    }

    @Nullable
    private ResponseProcessingStage logRequest(LocalRequest request) {
        ResponseProcessingStage stage = null;
        try {
            stage = logbook.process(request).write();
        } catch (Exception e) {
            log.warn("Unable to log request. Will skip the request & response logging step.", e);
        }
        return stage;
    }

    private static void logResponse(@Nullable final Logbook.ResponseProcessingStage stage, final RemoteResponse response) {
        if (stage != null) {
            try {
                stage.process(response).write();
            } catch (Exception e) {
                log.warn("Unable to log response. Will skip the response logging step.", e);
            }
        } else {
            log.warn("Unable to log response: ResponseProcessingStage is null. Will skip the response logging step.");
        }
    }

}
