package org.zalando.logbook.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import java.io.IOException;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class LogbookHttpAsyncResponseConsumer<T> extends ForwardingHttpAsyncResponseConsumer<T> {

    private final HttpAsyncResponseConsumer<T> consumer;
    private final boolean decompressResponse;
    private HttpResponse response;

    public LogbookHttpAsyncResponseConsumer(final HttpAsyncResponseConsumer<T> consumer, boolean decompressResponse) {
        this.consumer = consumer;
        this.decompressResponse = decompressResponse;
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
            if (stage != null) {
                stage.process(new RemoteResponse(response, decompressResponse)).write();
            } else {
                log.warn("Unable to log response: ResponseProcessingStage is null in HttpContext. Will skip the response logging step.");
            }
        } catch (final IOException e) {
            log.warn("Unable to log response. Will skip the response logging step.", e);
        }

        delegate().responseCompleted(context);
    }

    private ResponseProcessingStage find(final HttpContext context) {
        return (ResponseProcessingStage) context.getAttribute(Attributes.STAGE);
    }

}
