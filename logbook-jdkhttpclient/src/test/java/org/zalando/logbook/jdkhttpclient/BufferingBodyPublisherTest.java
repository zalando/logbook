package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class BufferingBodyPublisherTest {

    @Test
    void shouldCopyBytesWithoutChangingTheBufferForwardedToTheCaller() {
        final RecordingPublisher delegate = new RecordingPublisher(ByteBuffer.wrap("hello".getBytes(UTF_8)));
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(delegate);
        final ConsumingSubscriber subscriber = new ConsumingSubscriber();

        publisher.subscribe(subscriber);

        assertThat(publisher.contentLength()).isEqualTo(5);
        assertThat(subscriber.body()).isEqualTo("hello".getBytes(UTF_8));
        assertThat(publisher.getBody()).isEqualTo("hello".getBytes(UTF_8));
        assertThat(delegate.requested).isEqualTo(1);
    }

    @Test
    void shouldCaptureEmptyAndMultipleBuffers() {
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(new RecordingPublisher(
                ByteBuffer.allocate(0), ByteBuffer.wrap("he".getBytes(UTF_8)), ByteBuffer.wrap("llo".getBytes(UTF_8))));

        publisher.subscribe(new ConsumingSubscriber());

        assertThat(publisher.getBody()).isEqualTo("hello".getBytes(UTF_8));
    }

    @Test
    void shouldForwardErrorsAndCancellation() {
        final RecordingPublisher delegate = new RecordingPublisher(new IllegalStateException("failed"));
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(delegate);
        final ErrorSubscriber subscriber = new ErrorSubscriber();

        publisher.subscribe(subscriber);

        assertThat(subscriber.error).isInstanceOf(IllegalStateException.class);
        assertThat(delegate.requested).isEqualTo(1);
    }

    @Test
    void shouldForwardCancellationToTheDelegateSubscription() {
        final RecordingPublisher delegate = new RecordingPublisher(ByteBuffer.wrap("hello".getBytes(UTF_8)));
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(delegate);

        publisher.subscribe(new CancellingSubscriber());

        assertThat(delegate.cancelled).isTrue();
    }

    @Test
    void shouldReplaceCapturedBodyForEachSubscription() {
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(
                new RecordingPublisher(ByteBuffer.wrap("hello".getBytes(UTF_8))));

        publisher.subscribe(new ConsumingSubscriber());
        publisher.subscribe(new ConsumingSubscriber());

        assertThat(publisher.getBody()).isEqualTo("hello".getBytes(UTF_8));
    }

    private static final class RecordingPublisher implements HttpRequest.BodyPublisher {
        private final List<ByteBuffer> buffers;
        private final Throwable error;
        private final long contentLength;
        private long requested;
        private boolean cancelled;

        RecordingPublisher(final ByteBuffer... buffers) {
            this.buffers = List.of(buffers).stream()
                    .map(buffer -> ByteBuffer.wrap(copy(buffer)))
                    .toList();
            this.error = null;
            this.contentLength = this.buffers.stream().mapToLong(ByteBuffer::remaining).sum();
        }

        RecordingPublisher(final Throwable error) {
            this.buffers = List.of();
            this.error = error;
            this.contentLength = 0;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public void subscribe(final Subscriber<? super ByteBuffer> subscriber) {
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(final long count) {
                    requested += count;
                    if (error != null) {
                        subscriber.onError(error);
                    } else {
                        buffers.stream().map(ByteBuffer::duplicate).forEach(subscriber::onNext);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() {
                    cancelled = true;
                }
            });
        }
    }

    private static final class ConsumingSubscriber implements Subscriber<ByteBuffer> {
        private final List<Byte> bytes = new ArrayList<>();

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(1);
        }

        @Override
        public void onNext(final ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                bytes.add(buffer.get());
            }
        }

        @Override
        public void onError(final Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }

        byte[] body() {
            final byte[] body = new byte[bytes.size()];
            for (int index = 0; index < body.length; index++) {
                body[index] = bytes.get(index);
            }
            return body;
        }
    }

    private static final class ErrorSubscriber implements Subscriber<ByteBuffer> {
        private Throwable error;

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(1);
        }

        @Override
        public void onNext(final ByteBuffer buffer) {
        }

        @Override
        public void onError(final Throwable throwable) {
            error = throwable;
        }

        @Override
        public void onComplete() {
        }
    }

    private static final class CancellingSubscriber implements Subscriber<ByteBuffer> {
        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.cancel();
        }

        @Override
        public void onNext(final ByteBuffer buffer) {
        }

        @Override
        public void onError(final Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }

    private static byte[] copy(final ByteBuffer buffer) {
        final ByteBuffer copy = buffer.asReadOnlyBuffer();
        final byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return bytes;
    }
}
