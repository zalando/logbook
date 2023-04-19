package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * A response interceptor for synchronous responses. For {@link HttpAsyncClient} support, please use
 * {@link LogbookHttpAsyncResponseConsumer} instead.
 *
 * @see LogbookHttpRequestInterceptor
 * @see LogbookHttpAsyncResponseConsumer
 */
@API(status = EXPERIMENTAL)
public final class LogbookHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse original, EntityDetails entity, HttpContext context) throws IOException {
        final ResponseProcessingStage stage = find(context);
        stage.process(new RemoteResponse(original)).write();
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

}
