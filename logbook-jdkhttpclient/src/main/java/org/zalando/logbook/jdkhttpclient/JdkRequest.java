package org.zalando.logbook.jdkhttpclient;

import lombok.AllArgsConstructor;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.Origin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

final class JdkRequest implements org.zalando.logbook.HttpRequest {

    private final AtomicReference<State> state;
    private final HttpRequest request;

    private interface State {
        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpRequest request) throws IOException {
            return this;
        }

        HttpRequest getRequest();

        default byte[] getBody() {
            return new byte[0];
        }
    }

    @AllArgsConstructor
    private static final class Unbuffered implements State {
        private final HttpRequest request;

        @Override
        public State with() {
            return new Offering(request);
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }
    }

    @AllArgsConstructor
    private static final class Offering implements State {
        private final HttpRequest request;

        @Override
        public State without() {
            return new Unbuffered(request);
        }

        @Override
        public State buffer(final HttpRequest request) throws IOException {
            if (request.bodyPublisher().isEmpty()) {
                return new Passing(request);
            }

            final byte[] body = drain(request.bodyPublisher().get());

            final HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                    .method(request.method(), BodyPublishers.ofByteArray(body));

            request.headers().map().forEach((name, values) -> {
                for (final String value : values) {
                    builder.header(name, value);
                }
            });

            request.timeout().ifPresent(builder::timeout);
            request.version().ifPresent(builder::version);
            builder.expectContinue(request.expectContinue());

            final HttpRequest copy = builder.build();
            return new Buffering(copy, body);
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }
    }

    @AllArgsConstructor
    private static final class Buffering implements State {
        private final HttpRequest copy;
        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(copy, body);
        }

        @Override
        public HttpRequest getRequest() {
            return copy;
        }

        @Override
        public byte[] getBody() {
            return body;
        }
    }

    @AllArgsConstructor
    private static final class Ignoring implements State {
        private final HttpRequest copy;
        private final byte[] body;

        @Override
        public State with() {
            return new Buffering(copy, body);
        }

        @Override
        public HttpRequest getRequest() {
            return copy;
        }
    }

    @AllArgsConstructor
    private static final class Passing implements State {
        private final HttpRequest request;

        @Override
        public HttpRequest getRequest() {
            return request;
        }
    }

    JdkRequest(final HttpRequest request) {
        this.request = request;
        this.state = new AtomicReference<>(new Unbuffered(request));
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.method();
    }

    @Override
    public String getScheme() {
        return request.uri().getScheme();
    }

    @Override
    public String getHost() {
        return request.uri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.uri().getPort()).filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return request.uri().getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(request.uri().getQuery()).orElse("");
    }

    @Override
    public String getProtocolVersion() {
        return request.version()
                .map(v -> v == HttpClient.Version.HTTP_2 ? "HTTP/2" : "HTTP/1.1")
                .orElse("HTTP/1.1");
    }

    @Override
    public org.zalando.logbook.HttpHeaders getHeaders() {
        return org.zalando.logbook.HttpHeaders.of(request.headers().map());
    }

    @Override
    public String getContentType() {
        return request.headers().firstValue("Content-Type").orElse("");
    }

    @Override
    public Charset getCharset() {
        return request.headers().firstValue("Content-Type")
                .map(ContentType::parseCharset)
                .orElse(UTF_8);
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public org.zalando.logbook.HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public byte[] getBody() {
        return state.updateAndGet(throwingUnaryOperator(s -> s.buffer(request))).getBody();
    }

    HttpRequest toHttpRequest() {
        return state.get().getRequest();
    }

    private static byte[] drain(final HttpRequest.BodyPublisher publisher) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CompletableFuture<byte[]> future = new CompletableFuture<>();

        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(final Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(final ByteBuffer item) {
                final byte[] bytes = new byte[item.remaining()];
                item.get(bytes);
                out.write(bytes, 0, bytes.length);
            }

            @Override
            public void onError(final Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(out.toByteArray());
            }
        });

        try {
            return future.join();
        } catch (final CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Failed to drain request body", cause);
        }
    }
}
