package org.zalando.logbook.httpclient5;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class LogbookHttpAsyncResponseConsumer<T> extends ForwardingHttpAsyncResponseConsumer<T> {

    private final AsyncResponseConsumer<T> consumer;
    private final Function<T, byte[]> bodyExtractor;
    private final boolean decompressResponse;
    private HttpResponse response;
    private EntityDetails entityDetails;
    private ResponseProcessingStage stage;

    public LogbookHttpAsyncResponseConsumer(final AsyncResponseConsumer<T> consumer, final Function<T, byte[]> bodyExtractor, final boolean decompressResponse) {
        this.consumer = consumer;
        this.bodyExtractor = bodyExtractor;
        this.decompressResponse = decompressResponse;
    }

    @Override
    protected AsyncResponseConsumer<T> delegate() {
        return consumer;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context, FutureCallback<T> resultCallback) throws HttpException, IOException {
        this.stage = find(context);
        if (entityDetails == null) {
            processStage(response, null, null);
            delegate().consumeResponse(response, entityDetails, context, resultCallback);
        } else {
            this.response = response;
            this.entityDetails = entityDetails;
            delegate().consumeResponse(response, entityDetails, context, new LogbookFutureCallback(resultCallback));
        }
    }

    private void processStage(final HttpResponse response, final EntityDetails entityDetails, final ByteBuffer src) {
        if (stage == null) {
            log.warn("Unable to log response: ResponseProcessingStage is null in HttpContext. Will skip the response logging step.");
            return;
        }

        try {
            stage.process(new RemoteResponse(response, entityDetails, src, decompressResponse)).write();
        } catch (Exception e) {
            log.warn("Unable to log response. Will skip the response logging step.", e);
        }
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

    @RequiredArgsConstructor
    private final class LogbookFutureCallback implements FutureCallback<T> {

        @Delegate
        private final FutureCallback<T> delegate;

        @Override
        public void completed(final T result) {
            final byte[] body = bodyExtractor.apply(result);
            processStage(response, entityDetails, ByteBuffer.wrap(body));
            delegate.completed(result);
        }
    }
}
