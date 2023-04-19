package org.zalando.logbook.okhttp;

import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

    @Nonnull
    @Override
    public Response intercept(final Chain chain) throws IOException {
        final LocalRequest request = new LocalRequest(chain.request());
        final ResponseProcessingStage stage = logbook.process(request).write();
        final RemoteResponse response = new RemoteResponse(chain.proceed(request.toRequest()));
        stage.process(response).write();

        return response.toResponse();
    }

}
