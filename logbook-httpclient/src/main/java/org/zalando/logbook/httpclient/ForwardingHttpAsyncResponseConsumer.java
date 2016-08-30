package org.zalando.logbook.httpclient;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

abstract class ForwardingHttpAsyncResponseConsumer<T> implements HttpAsyncResponseConsumer<T> {

    protected abstract HttpAsyncResponseConsumer<T> delegate();

    @Override
    public void responseReceived(final HttpResponse response) throws IOException, HttpException {
        delegate().responseReceived(response);
    }

    @Override
    public void consumeContent(final ContentDecoder decoder, final IOControl control) throws IOException {
        delegate().consumeContent(decoder, control);
    }

    @Override
    public void responseCompleted(final HttpContext context) {
        delegate().responseCompleted(context);
    }

    @Override
    public boolean cancel() {
        return delegate().cancel();
    }

    @Override
    public boolean isDone() {
        return delegate().isDone();
    }

    @Override
    public T getResult() {
        return delegate().getResult();
    }

    @Override
    public void failed(final Exception e) {
        delegate().failed(e);
    }

    @Override
    public Exception getException() {
        return delegate().getException();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

}
