package org.zalando.logbook.okhttp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static okhttp3.HttpUrl.defaultPort;
import static okhttp3.RequestBody.create;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class LocalRequest implements HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final Request request;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer() throws IOException {
            return this;
        }

        Request getRequest();

        default byte[] getBody() {
            return new byte[0];
        }

    }

    @AllArgsConstructor
    private abstract class AbstractState implements State {

        @Override
        public Request getRequest() {
            return request;
        }

    }

    private final class Unbuffered extends AbstractState {

        @Override
        public State with() {
            return new Offering();
        }

    }

    private final class Offering extends AbstractState {

        @Override
        public State without() {
            return new Unbuffered();
        }

        @Override
        public State buffer() throws IOException {
            @Nullable final RequestBody entity = request.body();

            if (entity == null) {
                return new Passing();
            } else {
                final Buffer buffer = new Buffer();
                entity.writeTo(buffer);
                final byte[] body = buffer.readByteArray();

                final Request copy = request.newBuilder()
                        .method(request.method(), create(body, entity.contentType()))
                        .build();

                return new Buffering(copy, body);
            }
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        @Getter
        private final Request request;
        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(request, body);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        @Getter
        private final Request request;
        private final byte[] body;

        @Override
        public State with() {
            return new Buffering(request, body);
        }

    }

    private final class Passing extends AbstractState {

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
        return request.url().scheme();
    }

    @Override
    public String getHost() {
        return request.url().host();
    }

    @Override
    public Optional<Integer> getPort() {
        final int port = request.url().port();
        final int defaultPort = defaultPort(request.url().scheme());
        return port == defaultPort ? Optional.empty() : Optional.of(port);
    }

    @Override
    public String getPath() {
        return request.url().encodedPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(request.url().query()).orElse("");
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(request.headers().toMultimap());
    }

    @Override
    public String getContentType() {
        return contentType().map(MediaType::toString).orElse("");
    }

    @Override
    public Charset getCharset() {
        return contentType().map(MediaType::charset).orElse(UTF_8);
    }

    private Optional<MediaType> contentType() {
        return Optional.ofNullable(request.body())
                .map(RequestBody::contentType);
    }

    @Override
    public HttpRequest withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    Request toRequest() {
        return buffer().getRequest();
    }

    @Override
    public byte[] getBody() {
        return buffer().getBody();
    }

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(State::buffer));
    }

}
