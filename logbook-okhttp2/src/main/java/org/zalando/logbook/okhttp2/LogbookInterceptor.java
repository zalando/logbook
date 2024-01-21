package org.zalando.logbook.okhttp2;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
@Slf4j
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

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
            log.trace("Unable to log request: {}", e.getClass());
        }
        return stage;
    }

    private static void logResponse(@Nullable final Logbook.ResponseProcessingStage stage, final RemoteResponse response) {
        if (stage != null) {
            try {
                stage.process(response).write();
            } catch (Exception e) {
                log.trace("Unable to log response: {}", e.getClass());
            }
        } else {
            log.trace("Unable to log response: ResponseProcessingStage is null");
        }
    }

}
