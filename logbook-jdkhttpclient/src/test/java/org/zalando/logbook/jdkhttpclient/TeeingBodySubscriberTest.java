package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class TeeingBodySubscriberTest {

    @Test
    void shouldNotMutateBufferPositionForDelegate() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final AtomicInteger delegatePositionSeen = new AtomicInteger(-1);
        final AtomicInteger delegateLimitSeen = new AtomicInteger(-1);

        @SuppressWarnings("unchecked")
        final BodySubscriber<String> delegate = mock(BodySubscriber.class);

        final TeeingBodySubscriber<String> subscriber = new TeeingBodySubscriber<>(delegate, output);

        final byte[] data = "Hello, world!".getBytes(UTF_8);
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final int initialPosition = buffer.position();
        final int initialLimit = buffer.limit();

        subscriber.onNext(List.of(buffer));

        assertThat(output.toByteArray()).isEqualTo(data);
        // The original buffer passed to delegate should have its initial position and limit intact
        assertThat(buffer.position()).isEqualTo(initialPosition);
        assertThat(buffer.limit()).isEqualTo(initialLimit);

        verify(delegate).onNext(List.of(buffer));
    }

    @Test
    void shouldForwardLifecycleMethodsToDelegate() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        @SuppressWarnings("unchecked")
        final BodySubscriber<Void> delegate = mock(BodySubscriber.class);
        final Flow.Subscription subscription = mock(Flow.Subscription.class);
        final Throwable error = new RuntimeException("test error");
        final CompletionStage<Void> future = CompletableFuture.completedFuture(null);

        when(delegate.getBody()).thenReturn(future);

        final TeeingBodySubscriber<Void> subscriber = new TeeingBodySubscriber<>(delegate, output);

        subscriber.onSubscribe(subscription);
        verify(delegate).onSubscribe(subscription);

        subscriber.onError(error);
        verify(delegate).onError(error);

        subscriber.onComplete();
        verify(delegate).onComplete();

        assertThat(subscriber.getBody()).isSameAs(future);
    }
}
