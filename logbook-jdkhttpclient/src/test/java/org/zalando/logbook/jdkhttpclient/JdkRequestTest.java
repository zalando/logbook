package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Flow;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class JdkRequestTest {

    private JdkRequest unit(final HttpRequest request) {
        return new JdkRequest(request);
    }

    private HttpRequest get(final String uri) {
        return HttpRequest.newBuilder(URI.create(uri)).GET().build();
    }

    private HttpRequest post(final String uri, final String body) {
        return HttpRequest.newBuilder(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Test
    void shouldResolveOriginAndRemote() {
        final JdkRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getOrigin()).isEqualTo(org.zalando.logbook.Origin.LOCAL);
        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldRetrieveUriParts() {
        final JdkRequest unit = unit(get("http://localhost:8080/path?query=1"));

        assertThat(unit.getScheme()).isEqualTo("http");
        assertThat(unit.getHost()).isEqualTo("localhost");
        assertThat(unit.getPort()).contains(8080);
        assertThat(unit.getPath()).isEqualTo("/path");
        assertThat(unit.getQuery()).isEqualTo("query=1");
        assertThat(unit.getRequestUri()).isEqualTo("http://localhost:8080/path?query=1");
    }

    @Test
    void shouldHandleDefaultPortAndEmptyQuery() {
        final JdkRequest unit = unit(get("http://localhost/"));

        assertThat(unit.getPort()).isEmpty();
        assertThat(unit.getQuery()).isEmpty();
        assertThat(unit.getRequestUri()).isEqualTo("http://localhost/");
    }

    @Test
    void shouldRetrieveMethod() {
        assertThat(unit(get("http://localhost/")).getMethod()).isEqualTo("GET");
        assertThat(unit(post("http://localhost/", "test")).getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldResolveProtocolVersion() {
        final HttpRequest http1 = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        assertThat(unit(http1).getProtocolVersion()).isEqualTo("HTTP/1.1");

        final HttpRequest http2 = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .version(HttpClient.Version.HTTP_2)
                .build();
        assertThat(unit(http2).getProtocolVersion()).isEqualTo("HTTP/2");

        final HttpRequest defaultVer = HttpRequest.newBuilder(URI.create("http://localhost/")).build();
        assertThat(unit(defaultVer).getProtocolVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void shouldRetrieveHeaders() {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .header("X-Single", "one")
                .header("X-Multi", "val1")
                .header("X-Multi", "val2")
                .build();

        final JdkRequest unit = unit(request);
        assertThat(unit.getHeaders()).hasSize(2);
        assertThat(unit.getHeaders().get("x-single")).containsExactly("one");
        assertThat(unit.getHeaders().get("x-multi")).containsExactly("val1", "val2");
    }

    @Test
    void shouldResolveContentTypeAndCharset() {
        final HttpRequest req1 = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .header("Content-Type", "text/plain; charset=ISO-8859-1")
                .build();
        final JdkRequest unit1 = unit(req1);
        assertThat(unit1.getContentType()).isEqualTo("text/plain; charset=ISO-8859-1");
        assertThat(unit1.getCharset()).isEqualTo(ISO_8859_1);

        final HttpRequest req2 = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .header("Content-Type", "application/json")
                .build();
        final JdkRequest unit2 = unit(req2);
        assertThat(unit2.getContentType()).isEqualTo("application/json");
        assertThat(unit2.getCharset()).isEqualTo(UTF_8);

        final HttpRequest req3 = HttpRequest.newBuilder(URI.create("http://localhost/")).build();
        final JdkRequest unit3 = unit(req3);
        assertThat(unit3.getContentType()).isEmpty();
        assertThat(unit3.getCharset()).isEqualTo(UTF_8);
    }

    @Test
    void shouldReturnEmptyBodyUntilCaptured() throws IOException {
        final HttpRequest original = post("http://localhost/", "Hello, world!");
        final JdkRequest unit = unit(original);

        assertThat(unit.getBody()).isEmpty();
        assertThat(unit.toHttpRequest()).isSameAs(original);

        assertThat(new String(unit.withBody().getBody(), UTF_8)).isEqualTo("Hello, world!");
        assertThat(unit.toHttpRequest()).isNotSameAs(original);
    }

    @Test
    void shouldHandleRequestWithoutBodyPublisher() throws IOException {
        final HttpRequest original = get("http://localhost/");
        final JdkRequest unit = unit(original);

        assertThat(unit.withBody().getBody()).isEmpty();
        assertThat(unit.toHttpRequest()).isSameAs(original);
    }

    @Test
    void shouldBeSafeAgainstMultipleWithBodyAndWithoutBody() throws IOException {
        final HttpRequest original = post("http://localhost/", "Hello, world!");
        final JdkRequest unit = unit(original);

        unit.withBody().withBody();
        assertThat(unit.toHttpRequest()).isSameAs(original);
        assertThat(new String(unit.getBody(), UTF_8)).isEqualTo("Hello, world!");

        unit.withoutBody().withoutBody();
        assertThat(unit.getBody()).isEmpty();
        assertThat(unit.toHttpRequest()).isNotSameAs(original);

        unit.withBody();
        assertThat(new String(unit.getBody(), UTF_8)).isEqualTo("Hello, world!");
    }

    @Test
    void shouldPreserveAttributesOnRebuild() throws IOException {
        final HttpRequest original = HttpRequest.newBuilder(URI.create("http://localhost/path?q=1"))
                .method("POST", HttpRequest.BodyPublishers.ofString("test-body"))
                .header("X-Foo", "bar")
                .header("X-List", "a")
                .header("X-List", "b")
                .timeout(Duration.ofSeconds(12))
                .version(HttpClient.Version.HTTP_2)
                .expectContinue(true)
                .build();

        final JdkRequest unit = unit(original);
        unit.withBody().getBody();

        final HttpRequest rebuilt = unit.toHttpRequest();
        assertThat(rebuilt.uri()).isEqualTo(URI.create("http://localhost/path?q=1"));
        assertThat(rebuilt.method()).isEqualTo("POST");
        assertThat(rebuilt.headers().map()).containsEntry("X-Foo", List.of("bar"));
        assertThat(rebuilt.headers().map()).containsEntry("X-List", List.of("a", "b"));
        assertThat(rebuilt.timeout()).contains(Duration.ofSeconds(12));
        assertThat(rebuilt.version()).contains(HttpClient.Version.HTTP_2);
        assertThat(rebuilt.expectContinue()).isTrue();
        assertThat(rebuilt.bodyPublisher()).isPresent();
    }

    @Test
    void shouldHandleDrainingFailure() {
        final HttpRequest.BodyPublisher failingPublisher = new HttpRequest.BodyPublisher() {
            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public void subscribe(final Flow.Subscriber<? super ByteBuffer> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        subscriber.onError(new IOException("Simulated network read error"));
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };

        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .POST(failingPublisher)
                .build();

        final JdkRequest unit = unit(request);
        unit.withBody();

        assertThatThrownBy(unit::getBody)
                .isInstanceOf(IOException.class)
                .hasMessage("Simulated network read error");
    }

    @Test
    void shouldWrapNonIOExceptionDrainingFailure() {
        final HttpRequest.BodyPublisher runtimeErrorPublisher = new HttpRequest.BodyPublisher() {
            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public void subscribe(final Flow.Subscriber<? super ByteBuffer> subscriber) {
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        subscriber.onError(new IllegalStateException("Runtime failure"));
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };

        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost/"))
                .POST(runtimeErrorPublisher)
                .build();

        final JdkRequest unit = unit(request);
        unit.withBody();

        assertThatThrownBy(unit::getBody)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to drain request body")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
