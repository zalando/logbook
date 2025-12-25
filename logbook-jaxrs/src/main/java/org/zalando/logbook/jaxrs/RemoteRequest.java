package org.zalando.logbook.jaxrs;

import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.ofNullable;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.jaxrs.ByteStreams.toByteArray;

@AllArgsConstructor
final class RemoteRequest implements HttpRequest {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final ContainerRequestContext context;

    /**
     * Manages the lifecycle of HTTP request body buffering for JAX-RS requests.
     *
     * <h3>State Machine Overview</h3>
     *
     * The state machine controls when and how the request body is read and buffered,
     * with direct integration into the JAX-RS ContainerRequestContext:
     *
     * <pre>
     *     Unbuffered
     *       ↙     ↘
     *    with()  without()
     *     ↙         ↘
     *  Offering   Withouted
     *     ↓           ↑
     *  buffer()      with()
     *     ↓           |
     *  Buffering     (back to Offering)
     *     ↓
     *  without()
     *     ↓
     *  Ignoring ⇄ Buffering (via with())
     * </pre>
     *
     * <h3>State Descriptions</h3>
     *
     * <ul>
     * <li><b>Unbuffered</b>: Initial state. Body has not been read yet. Transitions occur when
     * logging preference is set:
     *   <ul>
     *   <li>{@code with()} → Offering: Body reading will be offered/logged</li>
     *   <li>{@code without()} → Withouted: Body reading explicitly rejected</li>
     *   </ul>
     * </li>
     *
     * <li><b>Offering</b>: Body logging is desired but not yet buffered. Waits for eager buffering:
     *   <ul>
     *   <li>{@code buffer()} → Buffering: Body is read, cached, and entity stream is replaced
     *   with buffered stream for JAX-RS container</li>
     *   </ul>
     * </li>
     *
     * <li><b>Withouted</b>: Body logging was explicitly rejected without buffering. Can return
     * to Offering if needed later:
     *   <ul>
     *   <li>{@code with()} → Offering: Body logging is now desired</li>
     *   </ul>
     * </li>
     *
     * <li><b>Buffering</b>: Body has been read from the stream and cached in memory. The JAX-RS
     * entity stream in the ContainerRequestContext has been replaced with a ByteArrayInputStream
     * allowing the body to be re-read. Provides buffered body and allows toggling visibility:
     *   <ul>
     *   <li>{@code without()} → Ignoring: Switch to hiding the buffered body from consumers</li>
     *   </ul>
     * </li>
     *
     * <li><b>Ignoring</b>: Body was buffered but is being hidden from downstream consumers.
     * The body remains cached internally but appears empty to callers. Can switch back:
     *   <ul>
     *   <li>{@code with()} → Buffering: Reveal the buffered body again</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <h3>JAX-RS Integration</h3>
     *
     * This state machine operates within the JAX-RS request lifecycle:
     * <ul>
     * <li>The buffered body is stored via {@link ContainerRequestContext#setEntityStream(java.io.InputStream)},
     * allowing other JAX-RS filters and resource methods to re-read the body</li>
     * <li>The entity stream replacement happens during the buffer() transition in Offering state</li>
     * <li>This enables logging while maintaining compatibility with resource methods that also
     * need to read the request body</li>
     * </ul>
     *
     * <h3>Key Design Points</h3>
     *
     * <ul>
     * <li><b>Lazy Buffering</b>: The body is not buffered until actually needed (when
     * {@code buffer()} is called), allowing efficient handling of requests where logging
     * is configured but the body is never read.</li>
     *
     * <li><b>State Reusability</b>: Once buffered, the same body can be shown/hidden multiple
     * times by transitioning between Buffering and Ignoring states.</li>
     *
     * <li><b>Eager Call on with()</b>: When {@code with()} is called (via
     * {@link RemoteRequest#withBody()}), buffering is eagerly triggered via the {@code expose()}
     * method to ensure the body is captured and the entity stream is properly replaced in the
     * JAX-RS context before other filters execute.</li>
     *
     * <li><b>Stream Replayability</b>: By caching the body bytes and replacing the entity stream,
     * the body can be "replayed" multiple times by different components in the JAX-RS pipeline.</li>
     *
     * <li><b>Deferred Buffering on Unbuffered</b>: Unlike other modules, buffering in
     * Unbuffered state does NOT happen when getBody() is called - it only happens when buffer()
     * is explicitly called (typically from expose()). This prevents automatic buffering unless
     * logging is explicitly enabled.</li>
     * </ul>
     */
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

        @Override
        public State without() {
            return new Withouted();
        }

    }

    private static final class Withouted implements State {

        @Override
        public State with() {
            return new Offering();
        }

    }

    private static final class Offering implements State {

        @Override
        public State buffer(final ContainerRequestContext context) throws IOException {
            return doBuffer(context);
        }

    }

    private static State doBuffer(final ContainerRequestContext context) throws IOException {
        final byte[] body = toByteArray(context.getEntityStream());
        context.setEntityStream(new ByteArrayInputStream(body));
        return new Buffering(body);
    }

    private static abstract class WithBody implements State {

        protected final byte[] body;

        protected WithBody(final byte[] body) {
            this.body = body;
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    private static final class Buffering extends WithBody {

        Buffering(final byte[] body) {
            super(body);
        }

        @Override
        public State without() {
            return new Ignoring(body);
        }

    }

    private static final class Ignoring extends WithBody {

        Ignoring(final byte[] body) {
            super(body);
        }

        @Override
        public byte[] getBody() {
            return new byte[0];
        }

        @Override
        public State with() {
            return new Buffering(body);
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
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(context.getHeaders());
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
        expose();
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
