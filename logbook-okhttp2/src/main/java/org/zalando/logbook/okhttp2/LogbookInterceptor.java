package org.zalando.logbook.okhttp2;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@AllArgsConstructor
public final class LogbookInterceptor implements Interceptor {

    private final Logbook logbook;

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final LocalRequest request = new LocalRequest(chain.request());
        final ResponseProcessingStage stage = logbook.process(request).write();
        final RemoteResponse response = new RemoteResponse(chain.proceed(request.toRequest()));
        stage.process(response).write();

        return response.toResponse();
    }

}
