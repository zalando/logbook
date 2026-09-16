package org.zalando.logbook.jdkhttpclient;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Origin;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Subscription;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class RemoteResponseTest {

    @Test
    void shouldMapResponseMetadataAndBody() throws Exception {
        final RemoteResponse logbookResponse = new RemoteResponse(response("created", HttpHeaders.of(
                java.util.Map.of("Content-Type", List.of("text/plain"), "X-Trace", List.of("one", "two")), (name, value) -> true)),
                handler("created"), false);

        assertThat(logbookResponse.getOrigin()).isEqualTo(Origin.REMOTE);
        assertThat(logbookResponse.getStatus()).isEqualTo(201);
        assertThat(logbookResponse.getProtocolVersion()).isEqualTo("HTTP_2");
        assertThat(logbookResponse.getReasonPhrase()).isEmpty();
        assertThat(logbookResponse.getHeaders().get("X-Trace")).containsExactly("one", "two");
        assertThat(logbookResponse.getCharset()).isEqualTo(UTF_8);
        assertThat(logbookResponse.getBody()).isEmpty();
        assertThat(logbookResponse.withBody().getBody()).isEqualTo("created".getBytes(UTF_8));
        assertThat(logbookResponse.withoutBody().getBody()).isEmpty();
    }

    @Test
    void shouldDecompressOnlyEnabledGzipResponses() throws Exception {
        final byte[] compressed = gzip("created");

        assertThat(new RemoteResponse(response(compressed, headers("gzip")), handler(compressed), true).withBody().getBody())
                .isEqualTo("created".getBytes(UTF_8));
        assertThat(new RemoteResponse(response(compressed, headers("X-GZip")), handler(compressed), true).withBody().getBody())
                .isEqualTo("created".getBytes(UTF_8));
        assertThat(new RemoteResponse(response(compressed, headers("br")), handler(compressed), true).withBody().getBody())
                .isEqualTo(compressed);
        assertThat(new RemoteResponse(response(compressed, headers("gzip")), handler(compressed), false).withBody().getBody())
                .isEqualTo(compressed);
    }

    @Test
    void shouldDecompressWhenAnyContentEncodingValueIsGzip() throws Exception {
        final byte[] compressed = gzip("created");
        final HttpHeaders headers = HttpHeaders.of(java.util.Map.of("Content-Encoding", List.of("br", "X-GZip")),
                (name, value) -> true);

        assertThat(new RemoteResponse(response(compressed, headers), handler(compressed), true).withBody().getBody())
                .isEqualTo("created".getBytes(UTF_8));
    }

    @Test
    void shouldNotDecompressAnExcludedBody() throws Exception {
        final RemoteResponse response = new RemoteResponse(response("not-compressed", headers("gzip")),
                handler("not-compressed"), true);

        assertThat(response.withoutBody().getBody()).isEmpty();
    }

    private static HttpHeaders headers(final String contentEncoding) {
        return HttpHeaders.of(java.util.Map.of("Content-Encoding", List.of(contentEncoding)), (name, value) -> true);
    }

    private static BufferingBodyHandler<String> handler(final String value) {
        return handler(value.getBytes(UTF_8));
    }

    private static BufferingBodyHandler<String> handler(final byte[] body) {
        final BufferingBodyHandler<String> handler = new BufferingBodyHandler<>(info -> HttpResponse.BodySubscribers.replacing("caller-value"));
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(final long count) {
            }

            @Override
            public void cancel() {
            }
        });
        subscriber.onNext(List.of(ByteBuffer.wrap(body)));
        subscriber.onComplete();
        return handler;
    }

    private static HttpResponse<byte[]> response(final String body, final HttpHeaders headers) {
        return response(body.getBytes(UTF_8), headers);
    }

    private static HttpResponse<byte[]> response(final byte[] body, final HttpHeaders headers) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 201;
            }

            @Override
            public HttpRequest request() {
                return HttpRequest.newBuilder(URI.create("https://example.org/orders")).GET().build();
            }

            @Override
            public Optional<HttpResponse<byte[]>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return headers;
            }

            @Override
            public byte[] body() {
                return body;
            }

            @Override
            public Optional<javax.net.ssl.SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return URI.create("https://example.org/orders");
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_2;
            }
        };
    }

    private static byte[] gzip(final String value) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(value.getBytes(UTF_8));
        }
        return output.toByteArray();
    }

    private static final class ResponseInfo implements HttpResponse.ResponseInfo {
        @Override
        public int statusCode() {
            return 201;
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_2;
        }
    }
}
