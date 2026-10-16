package org.zalando.logbook.jdkhttpclient;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicBoolean;

final class BufferingBodyPublisher implements HttpRequest.BodyPublisher {

    private final HttpRequest.BodyPublisher delegate;
    private ByteArrayOutputStream body = new ByteArrayOutputStream();
    private Runnable completed = () -> { };
    private Runnable incomplete = () -> { };

    BufferingBodyPublisher(final HttpRequest.BodyPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

    @Override
    public void subscribe(final Subscriber<? super ByteBuffer> subscriber) {
        final ByteArrayOutputStream capture = new ByteArrayOutputStream();
        final AtomicBoolean terminal = new AtomicBoolean();
        delegate.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(final Subscription subscription) {
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(final long count) {
                        subscription.request(count);
                    }

                    @Override
                    public void cancel() {
                        terminal.set(true);
                        incomplete.run();
                        subscription.cancel();
                    }
                });
            }

            @Override
            public void onNext(final ByteBuffer item) {
                final ByteBuffer copy = item.asReadOnlyBuffer();
                final byte[] bytes = new byte[copy.remaining()];
                copy.get(bytes);
                capture.writeBytes(bytes);
                subscriber.onNext(item);
            }

            @Override
            public void onError(final Throwable throwable) {
                if (terminal.compareAndSet(false, true)) {
                    incomplete.run();
                }
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                if (terminal.compareAndSet(false, true)) {
                    body = capture;
                    completed.run();
                }
                subscriber.onComplete();
            }
        });
    }

    void onComplete(final Runnable completed) {
        this.completed = completed;
    }

    void onIncomplete(final Runnable incomplete) {
        this.incomplete = incomplete;
    }

    byte[] getBody() {
        return body.toByteArray();
    }
}
