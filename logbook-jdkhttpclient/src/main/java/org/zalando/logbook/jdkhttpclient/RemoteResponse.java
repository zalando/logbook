package org.zalando.logbook.jdkhttpclient;

import lombok.AllArgsConstructor;
import org.zalando.logbook.ContentType;
import org.zalando.logbook.Origin;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

final class RemoteResponse implements org.zalando.logbook.HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
    private final HttpResponse<?> response;
    private final byte[] body;

    private interface State {
        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final byte[] body) {
            return this;
        }

        default byte[] getBody() {
            return new byte[0];
        }
    }

    private static final class Unbuffered implements State {
        @Override
        public State with() {
            return new Offering();
        }
    }

    private static final class Offering implements State {
        @Override
        public State without() {
            return new Unbuffered();
        }

        @Override
        public State buffer(final byte[] body) {
            return new Buffering(body);
        }
    }

    @AllArgsConstructor
    private static final class Buffering implements State {
        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(body);
        }

        @Override
        public byte[] getBody() {
            return body;
        }
    }

    @AllArgsConstructor
    private static final class Ignoring implements State {
        private final byte[] body;

        @Override
        public State with() {
            return new Buffering(body);
        }
    }

    RemoteResponse(final HttpResponse<?> response, final byte[] body) {
        this.response = Objects.requireNonNull(response);
        this.body = Objects.requireNonNull(body);
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public int getStatus() {
        return response.statusCode();
    }

    @Override
    public String getProtocolVersion() {
        return response.version() == HttpClient.Version.HTTP_2 ? "HTTP/2" : "HTTP/1.1";
    }

    @Override
    public org.zalando.logbook.HttpHeaders getHeaders() {
        return org.zalando.logbook.HttpHeaders.of(response.headers().map());
    }

    @Override
    public String getContentType() {
        return response.headers().firstValue("Content-Type").orElse("");
    }

    @Override
    public Charset getCharset() {
        return response.headers().firstValue("Content-Type")
                .map(ContentType::parseCharset)
                .orElse(UTF_8);
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public org.zalando.logbook.HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public byte[] getBody() {
        return state.updateAndGet(s -> s.buffer(body)).getBody();
    }
}
