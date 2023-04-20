package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class LogbookHttpAsyncResponseConsumer<T> extends ForwardingHttpAsyncResponseConsumer<T> {

    private final HttpAsyncResponseConsumer<T> consumer;
    private HttpResponse response;

    public LogbookHttpAsyncResponseConsumer(final HttpAsyncResponseConsumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected HttpAsyncResponseConsumer<T> delegate() {
        return consumer;
    }

    @Override
    public void responseReceived(final HttpResponse response) throws IOException, HttpException {
        this.response = response;
        delegate().responseReceived(response);
    }

    @Override
    public void responseCompleted(final HttpContext context) {
        final ResponseProcessingStage stage = find(context);

        try {
            stage.process(new RemoteResponse(response)).write();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        delegate().responseCompleted(context);
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

}
