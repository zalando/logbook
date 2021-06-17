package org.zalando.logbook.httpclient5;

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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class LogbookHttpAsyncResponseConsumer<T> extends ForwardingHttpAsyncResponseConsumer<T> {

    private final AsyncResponseConsumer<T> consumer;
    private HttpResponse response;
    private EntityDetails entityDetails;
    private ResponseProcessingStage stage;

    public LogbookHttpAsyncResponseConsumer(final AsyncResponseConsumer<T> consumer) {
        this.consumer = consumer;
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
            stage.process(new RemoteResponse(response)).write();
        } else {
            this.response = response;
            this.entityDetails = entityDetails;
        }
        delegate().consumeResponse(response, entityDetails, context, resultCallback);
    }

    @Override
    public void consume(ByteBuffer src) throws IOException {
        stage.process(new RemoteResponse(this.response, this.entityDetails, src)).write();
        delegate().consume(src);
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }
}
