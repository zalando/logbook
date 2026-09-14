package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Origin;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class LocalRequestTest {

    @Test
    void shouldMapRequestMetadataHeadersAndDefaultCharset() throws Exception {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.org:8443/orders?limit=1"))
                .header("X-Trace", "one")
                .header("X-Trace", "two")
                .POST(HttpRequest.BodyPublishers.ofString("created"))
                .build();
        final BufferingBodyPublisher publisher = new BufferingBodyPublisher(request.bodyPublisher().orElseThrow());
        final LocalRequest logbookRequest = new LocalRequest(request, publisher);

        publisher.subscribe(new DiscardingSubscriber());

        assertThat(logbookRequest.getOrigin()).isEqualTo(Origin.LOCAL);
        assertThat(logbookRequest.getMethod()).isEqualTo("POST");
        assertThat(logbookRequest.getProtocolVersion()).isEqualTo("HTTP/1.1");
        assertThat(logbookRequest.getRemote()).isEqualTo("localhost");
        assertThat(logbookRequest.getScheme()).isEqualTo("https");
        assertThat(logbookRequest.getHost()).isEqualTo("example.org");
        assertThat(logbookRequest.getPort()).contains(8443);
        assertThat(logbookRequest.getPath()).isEqualTo("/orders");
        assertThat(logbookRequest.getQuery()).isEqualTo("limit=1");
        assertThat(logbookRequest.getHeaders().get("X-Trace")).containsExactly("one", "two");
        assertThat(logbookRequest.getCharset()).isEqualTo(UTF_8);
        assertThat(logbookRequest.getBody()).isEmpty();
        assertThat(logbookRequest.withBody().getBody()).isEqualTo("created".getBytes(UTF_8));
        assertThat(logbookRequest.withoutBody().getBody()).isEmpty();
    }

    @Test
    void shouldUseContentTypeForTypeAndCharset() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://example.org/orders"))
                .header("Content-Type", "text/plain; charset=ISO-8859-1")
                .GET()
                .build();
        final LocalRequest logbookRequest = new LocalRequest(request, new BufferingBodyPublisher(HttpRequest.BodyPublishers.noBody()));

        assertThat(logbookRequest.getContentType()).isEqualTo("text/plain");
        assertThat(logbookRequest.getCharset()).isEqualTo(ISO_8859_1);
        assertThat(logbookRequest.getPort()).isEmpty();
    }

    private static final class DiscardingSubscriber implements Subscriber<ByteBuffer> {
        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final ByteBuffer item) {
        }

        @Override
        public void onError(final Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }
}
