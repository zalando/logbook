package org.zalando.logbook.httpclient;

import static org.apiguardian.api.API.Status.STABLE;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

/**
 * A response interceptor for synchronous responses. For {@link HttpAsyncClient} support, please use {@link LogbookHttpAsyncResponseConsumer} instead.
 *
 * @see LogbookHttpRequestInterceptor
 * @see LogbookHttpAsyncResponseConsumer
 */
@API(status = STABLE)
public final class LogbookHttpResponseInterceptor implements HttpResponseInterceptor {

    private final boolean decompressResponse;

    public LogbookHttpResponseInterceptor(boolean decompressResponse) {
        this.decompressResponse = decompressResponse;
    }

    @Override
    public void process(final HttpResponse original, final HttpContext context) throws IOException {
        final ResponseProcessingStage stage = find(context);
        stage.process(new RemoteResponse(original, decompressResponse)).write();
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

}
