package org.zalando.logbook.okhttp;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

    public LogbookInterceptor(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final LocalRequest request = new LocalRequest(chain.request());
        final ResponseProcessingStage stage = logbook.process(request).write();
        final RemoteResponse response = new RemoteResponse(chain.proceed(request.toRequest()));
        stage.process(response).write();

        return response.toResponse();
    }

}
