package org.zalando.logbook.jaxrs;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.client.ClientRequestContext;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.ofNullable;

@AllArgsConstructor
final class LocalRequest implements HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final ClientRequestContext context;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ClientRequestContext context) {
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
        public State buffer(final ClientRequestContext context) {
            final TeeOutputStream stream = new TeeOutputStream(context.getEntityStream());
            context.setEntityStream(stream);
            return new Buffering(stream);
        }

    }

    @AllArgsConstructor
    private static final class Buffering implements State {

        private final TeeOutputStream stream;

        @Override
        public State without() {
            return new Ignoring(stream);
        }

        @Override
        public byte[] getBody() {
            return stream.toByteArray();
        }

    }

    @AllArgsConstructor
    private static final class Ignoring implements State {

        private final TeeOutputStream stream;

        @Override
        public State with() {
            return new Buffering(stream);
        }

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
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getUri().getScheme();
    }

    @Override
    public String getHost() {
        return context.getUri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return HttpMessages.getPort(context.getUri());
    }

    @Override
    public String getPath() {
        return context.getUri().getPath();
    }

    @Override
    public String getQuery() {
        return ofNullable(context.getUri().getQuery()).orElse("");
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(context.getStringHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return context.getStringHeaders().getFirst("Content-Type");
    }

    @Override
    public Charset getCharset() {
        return HttpMessages.getCharset(context.getMediaType());
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

    void expose() {
        state.updateAndGet(state -> state.buffer(context));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
