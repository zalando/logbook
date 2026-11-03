package org.zalando.logbook.jdkhttpclient;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Subscription;

final class BufferingBodyHandler<T> implements HttpResponse.BodyHandler<T> {

    private final HttpResponse.BodyHandler<T> delegate;
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private volatile byte[] body;

    BufferingBodyHandler(final HttpResponse.BodyHandler<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(final HttpResponse.ResponseInfo responseInfo) {
        final HttpResponse.BodySubscriber<T> subscriber = delegate.apply(responseInfo);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        return new HttpResponse.BodySubscriber<>() {
            @Override
            public CompletionStage<T> getBody() {
                return subscriber.getBody();
            }

            @Override
            public void onSubscribe(final Subscription subscription) {
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(final List<ByteBuffer> items) {
                for (final ByteBuffer item : items) {
                    final ByteBuffer copy = item.asReadOnlyBuffer();
                    final byte[] bytes = new byte[copy.remaining()];
                    copy.get(bytes);
                    buffer.writeBytes(bytes);
                }
                subscriber.onNext(items);
            }

            @Override
            public void onError(final Throwable throwable) {
                subscriber.onError(throwable);
                completion.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
                body = buffer.toByteArray();
                completion.complete(null);
            }
        };
    }

    byte[] getBody() {
        return body == null ? new byte[0] : body.clone();
    }

    CompletionStage<Void> completion() {
        return completion;
    }
}
