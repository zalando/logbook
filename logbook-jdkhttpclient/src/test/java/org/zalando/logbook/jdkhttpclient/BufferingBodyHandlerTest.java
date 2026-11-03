package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import java.net.http.HttpHeaders;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Subscription;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class BufferingBodyHandlerTest {

    @Test
    void shouldCaptureBytesAndPreserveTheCallersNonByteArrayResult() {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> new StringSubscriber());
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());

        subscriber.onSubscribe(new RequestingSubscription());
        subscriber.onNext(List.of(ByteBuffer.wrap("res".getBytes(UTF_8)), ByteBuffer.wrap("ponse".getBytes(UTF_8))));
        subscriber.onComplete();

        assertThat(subscriber.getBody().toCompletableFuture().join()).isEqualTo("caller-value");
        assertThat(handler.getBody()).isEqualTo("response".getBytes(UTF_8));
        assertThat(handler.completion().toCompletableFuture()).isCompleted();
    }

    @Test
    void shouldExposeEmptyBodyAfterExceptionalTerminalSignal() {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> new StringSubscriber());
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());
        final IllegalStateException error = new IllegalStateException("failed");

        subscriber.onSubscribe(new RequestingSubscription());
        subscriber.onError(error);

        assertThat(handler.getBody()).isEmpty();
        assertThat(subscriber.getBody().toCompletableFuture()).isCompletedExceptionally();
        assertThat(handler.completion().toCompletableFuture()).isCompletedExceptionally();
    }

    @Test
    void shouldHideBodyBeforeATerminalSignal() {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> new StringSubscriber());

        handler.apply(new ResponseInfo());

        assertThat(handler.getBody()).isEmpty();
    }

    @Test
    void shouldForwardEmptyBodyAndSubscriptionRequests() {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> new StringSubscriber());
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());
        final TrackingSubscription subscription = new TrackingSubscription();

        subscriber.onSubscribe(subscription);
        subscriber.onNext(List.of());
        subscriber.onComplete();

        assertThat(subscription.requested).isEqualTo(1);
        assertThat(handler.getBody()).isEmpty();
    }

    @Test
    void shouldForwardCancellationToTheDelegateSubscription() {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> new CancellingStringSubscriber());
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());
        final TrackingSubscription subscription = new TrackingSubscription();

        subscriber.onSubscribe(subscription);

        assertThat(subscription.cancelled).isTrue();
    }

    private static final class StringSubscriber implements HttpResponse.BodySubscriber<String> {
        private final CompletableFuture<String> body = new CompletableFuture<>();
        private Subscription subscription;

        @Override
        public CompletionStage<String> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(final List<ByteBuffer> buffers) {
            buffers.forEach(ByteBuffer::get);
        }

        @Override
        public void onError(final Throwable throwable) {
            body.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            body.complete("caller-value");
        }
    }

    private static final class RequestingSubscription implements Subscription {
        @Override
        public void request(final long count) {
        }

        @Override
        public void cancel() {
        }
    }

    private static final class TrackingSubscription implements Subscription {
        private long requested;
        private boolean cancelled;

        @Override
        public void request(final long count) {
            requested += count;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private static final class CancellingStringSubscriber implements HttpResponse.BodySubscriber<String> {
        @Override
        public CompletionStage<String> getBody() {
            return CompletableFuture.completedFuture("caller-value");
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.cancel();
        }

        @Override
        public void onNext(final List<ByteBuffer> buffers) {
        }

        @Override
        public void onError(final Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }

    private static final class ResponseInfo implements HttpResponse.ResponseInfo {
        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
