package org.zalando.logbook.spring;

import lombok.AllArgsConstructor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

@AllArgsConstructor
final class RemoteResponse implements HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
    private final ClientHttpResponse response;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ClientHttpResponse response) throws IOException {
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
        public State buffer(final ClientHttpResponse response) throws IOException {
            InputStream responseBodyStream = response.getBody();
            responseBodyStream.mark(Integer.MAX_VALUE);
            byte[] data = ByteStreams.toByteArray(responseBodyStream);
            responseBodyStream.reset();
            return new Buffering(data);
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        private final byte[] body;

        @Override
        public State without() {
            return new Ignoring(this);
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        private final Buffering buffering;

        @Override
        public State with() {
            return buffering;
        }

    }

    private static final class Passing implements State {

    }

    @Override
    public int getStatus() {
        try {
            return response.getRawStatusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.getHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    @Override
    public byte[] getBody() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(response))).getBody();
    }
}
