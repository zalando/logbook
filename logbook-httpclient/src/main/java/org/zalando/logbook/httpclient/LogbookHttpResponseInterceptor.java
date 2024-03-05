package org.zalando.logbook.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.STABLE;

/**
 * A response interceptor for synchronous responses. For {@link HttpAsyncClient} support, please use
 * {@link LogbookHttpAsyncResponseConsumer} instead.
 *
 * @see LogbookHttpRequestInterceptor
 * @see LogbookHttpAsyncResponseConsumer
 */
@API(status = STABLE)
@Slf4j
public final class LogbookHttpResponseInterceptor implements HttpResponseInterceptor {

    private final boolean decompressResponse;

    public LogbookHttpResponseInterceptor() {
        this(false);
    }
    public LogbookHttpResponseInterceptor(boolean decompressResponse) {
        this.decompressResponse = decompressResponse;
    }

    @Override
    public void process(final HttpResponse original, final HttpContext context) throws IOException {
        try {
            doProcess(original, context);
        } catch (Exception e) {
            log.trace("Unable to log response: {}", e.getClass());
        }
    }

    private void doProcess(HttpResponse original, HttpContext context) throws IOException {
        final ResponseProcessingStage stage = find(context);
        if (stage != null) {
            stage.process(new RemoteResponse(original, decompressResponse)).write();
        } else {
            log.trace("Unable to log response: ResponseProcessingStage is null in HttpContext");
        }
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

}
