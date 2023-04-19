package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.client.ClientResponseContext;
import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.jaxrs.ByteStreams.toByteArray;

@AllArgsConstructor
final class RemoteResponse implements HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final ClientResponseContext context;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ClientResponseContext context) throws IOException {
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
        public State buffer(
                final ClientResponseContext context) throws IOException {

            try (InputStream entityStream = context.getEntityStream()) {
                final byte[] body = toByteArray(entityStream);
                context.setEntityStream(new ByteArrayInputStream(body));
                return new Buffering(body);
            }
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

    @Override
    public int getStatus() {
        return context.getStatus();
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
        return HttpHeaders.of(context.getHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Objects.toString(context.getMediaType(), null);
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
    }

    @Override
    public HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    void expose() {
        state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(context)));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
