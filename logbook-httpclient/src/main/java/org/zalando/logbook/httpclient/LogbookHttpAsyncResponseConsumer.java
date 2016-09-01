package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.zalando.logbook.Correlator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

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
        findCorrelator(context).ifPresent(correlator -> {
            try {
                correlator.write(new RemoteResponse(response));
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        delegate().responseCompleted(context);
    }

    private Optional<Correlator> findCorrelator(final HttpContext context) {
        return Optional.ofNullable(context.getAttribute(Attributes.CORRELATOR)).map(Correlator.class::cast);
    }

}
