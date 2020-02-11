package org.zalando.logbook.jaxrs;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerResponseContext;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
final class LocalResponse implements HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final ContainerResponseContext context;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final ContainerResponseContext context) {
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
        public State buffer(final ContainerResponseContext context) {
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
            return new Ignoring(this);
        }

        @Override
        public byte[] getBody() {
            return stream.toByteArray();
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
        return Origin.LOCAL;
    }

    @Override
    public int getStatus() {
        return context.getStatus();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return context.getStringHeaders();
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
        state.updateAndGet(state -> state.buffer(context));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
