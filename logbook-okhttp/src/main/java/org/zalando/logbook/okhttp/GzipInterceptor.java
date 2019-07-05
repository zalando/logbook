package org.zalando.logbook.okhttp;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import org.apiguardian.api.API;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static okio.Okio.buffer;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class GzipInterceptor implements Interceptor {

    @Nonnull
    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Response response = chain.proceed(chain.request());

        if (isContentEncodingGzip(response)) {
            return response.newBuilder()
                    .headers(response.headers()
                            .newBuilder()
                            .removeAll("Content-Encoding")
                            .removeAll("Content-Length")
                            .build())
                    .body(new RealResponseBody(
                            response.header("Content-Type"), -1L,
                            buffer(new GzipSource(requireNonNull(response.body(), "body").source()))))
                    .build();
        }

        return response;
    }

    private boolean isContentEncodingGzip(final Response response) {
        return "gzip".equalsIgnoreCase(response.header("Content-Encoding"));
    }

}
