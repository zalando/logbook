package org.zalando.logbook.okhttp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static okhttp3.ResponseBody.create;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class RemoteResponse implements HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final Response response;

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

        Response getResponse();

        default byte[] getBody() {
            return new byte[0];
        }

    }

    private abstract class AbstractState implements State {

        @Override
        public Response getResponse() {
            return response;
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
            final ResponseBody entity = requireNonNull(response.body(),
                    "Body is never null for normal responses");

            if (entity.contentLength() == 0L) {
                return new Passing();
            } else {
                final byte[] body = entity.bytes();

                final Response copy = response.newBuilder()
                        .body(create(body, entity.contentType()))
                        .build();

                return new Buffering(copy, body);
            }
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        @Getter
        private final Response response;
        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(response, body);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        @Getter
        private final Response response;
        private final byte[] body;

        @Override
        public State with() {
            return new Buffering(response, body);
        }

    }

    private final class Passing extends AbstractState {

    }

    @Override
    public int getStatus() {
        return response.code();
    }

    @Override
    public String getProtocolVersion() {
        // see https://tools.ietf.org/html/rfc7230#section-2.6
        return response.protocol().toString().toUpperCase(Locale.ROOT);
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.headers().toMultimap());
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
        return Optional.ofNullable(response.body())
                .map(ResponseBody::contentType);
    }

    @Override
    public HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    Response toResponse() {
        return buffer().getResponse();
    }

    @Override
    public byte[] getBody() {
        return buffer().getBody();
    }

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(State::buffer));
    }

}
