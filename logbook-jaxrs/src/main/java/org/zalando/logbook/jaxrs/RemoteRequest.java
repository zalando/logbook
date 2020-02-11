package org.zalando.logbook.jaxrs;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.ofNullable;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.jaxrs.ByteStreams.toByteArray;

@AllArgsConstructor
final class RemoteRequest implements HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final ContainerRequestContext context;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ContainerRequestContext context) throws IOException {
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
                final ContainerRequestContext context) throws IOException {

            final byte[] body = toByteArray(context.getEntityStream());
            context.setEntityStream(new ByteArrayInputStream(body));
            return new Buffering(body);
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
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getRemote() {
        // TODO find remote ip
        return context.getUriInfo().getRequestUri().getAuthority();
    }

    @Override
    public String getMethod() {
        return context.getMethod();
    }

    @Override
    public String getScheme() {
        return context.getUriInfo().getRequestUri().getScheme();
    }

    @Override
    public String getHost() {
        return context.getUriInfo().getRequestUri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return HttpMessages.getPort(context.getUriInfo().getRequestUri());
    }

    @Override
    public String getPath() {
        return context.getUriInfo().getRequestUri().getPath();
    }

    @Override
    public String getQuery() {
        return ofNullable(context.getUriInfo().getRequestUri().getQuery()).orElse("");
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getHeaders();
    }

    @Nullable
    @Override
    public String getContentType() {
        return context.getHeaders().getFirst("Content-Type");
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
        state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(context)));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
