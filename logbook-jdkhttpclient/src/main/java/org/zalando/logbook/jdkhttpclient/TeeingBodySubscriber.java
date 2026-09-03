package org.zalando.logbook.jdkhttpclient;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class TeeingBodySubscriber<T> implements BodySubscriber<T> {

    private final BodySubscriber<T> delegate;
    private final ByteArrayOutputStream output;

    TeeingBodySubscriber(final BodySubscriber<T> delegate, final ByteArrayOutputStream output) {
        this.delegate = Objects.requireNonNull(delegate);
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public CompletionStage<T> getBody() {
        return delegate.getBody();
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext(final List<ByteBuffer> item) {
        for (final ByteBuffer buffer : item) {
            final ByteBuffer duplicate = buffer.duplicate();
            final byte[] bytes = new byte[duplicate.remaining()];
            duplicate.get(bytes);
            output.write(bytes, 0, bytes.length);
        }
        delegate.onNext(item);
    }

    @Override
    public void onError(final Throwable throwable) {
        delegate.onError(throwable);
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
    }
}
