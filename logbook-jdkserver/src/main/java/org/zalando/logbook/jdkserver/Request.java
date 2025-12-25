package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PROTECTED;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;
import static org.zalando.logbook.jdkserver.ByteStreams.toByteArray;

final class Request implements HttpRequest {

    private final AtomicReference<State> state;

    private final HttpExchange httpExchange;

    /**
     * Manages the lifecycle of HTTP request body buffering.
     *
     * <h3>State Machine Overview</h3>
     *
     * The state machine controls when and how the request body is read and buffered:
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
     *   <li>{@code buffer()} → Buffering: Body is eagerly buffered (lazy evaluation)</li>
     *   </ul>
     * </li>
     *
     * <li><b>Offering</b>: Body logging is desired but not yet buffered. Waits for eager buffering:
     *   <ul>
     *   <li>{@code buffer()} → Buffering: Body is read and cached</li>
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
     * <li><b>Buffering</b>: Body has been read from the stream and cached in memory. Provides
     * buffered body and allows toggling visibility:
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
     * {@link Request#withBody()}), buffering is eagerly triggered to ensure the body is
     * captured before it can be consumed elsewhere.</li>
     *
     * <li><b>Stream Replayability</b>: By caching the body bytes, the stream can be
     * "replayed" multiple times without re-reading from the network.</li>
     * </ul>
     */
    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(final HttpExchange exchange) throws IOException {
            return this;
        }

        default InputStream getInputStream(final HttpExchange exchange) throws IOException {
            return exchange.getRequestBody();
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

        @Override
        public State buffer(HttpExchange exchange) throws IOException {
            return doBuffer(exchange);
        }

    }

    private static final class Withouted implements State {
        // Body has been explicitly discarded without buffering

        @Override
        public State with() {
            return new Offering();
        }
    }

    private static final class Offering implements State {

        @Override
        public State buffer(HttpExchange exchange) throws IOException {
            return doBuffer(exchange);
        }
    }

    private static State doBuffer(final HttpExchange exchange) throws IOException {
        return new Buffering(toByteArray(exchange.getRequestBody()));
    }

    @AllArgsConstructor
    private static abstract class Streaming implements State {

        @Getter(PROTECTED)
        protected final ByteArrayInputStream stream;

    }

    private static abstract class WithBody extends Streaming {

        protected final byte[] body;

        protected WithBody(final byte[] body, final ByteArrayInputStream stream) {
            super(stream);
            this.body = body;
        }

        @Override
        public byte[] getBody() {
            return body;
        }

    }

    private static final class Buffering extends WithBody {

        Buffering(final byte[] body) {
            this(body, new ByteArrayInputStream(body));
        }

        Buffering(final byte[] body, final ByteArrayInputStream stream) {
            super(body, stream);
        }

        @Override
        public InputStream getInputStream(final HttpExchange exchange) throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public State without() {
            return new Ignoring(body);
        }

    }

    private static final class Ignoring extends WithBody {

        Ignoring(final byte[] body) {
            super(body, new ByteArrayInputStream(body));
        }

        @Override
        public InputStream getInputStream(final HttpExchange exchange) throws IOException {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public byte[] getBody() {
            return new byte[0];
        }

        @Override
        public State with() {
            return new Buffering(body, getStream());
        }

    }

    public Request(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.state = new AtomicReference<>(new Unbuffered());
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();
        for (Map.Entry<String, List<String>> e : httpExchange.getRequestHeaders().entrySet()) {
            headers = headers.update(e.getKey(), e.getValue());
        }
        return headers;
    }

    @Override
    public String getRemote() {
        return httpExchange.getRemoteAddress().getHostString();
    }

    @Override
    public String getMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public String getScheme() {
        return notNull(httpExchange.getRequestURI().getScheme());
    }

    @Override
    public String getHost() {
        return httpExchange.getLocalAddress().getHostString();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(httpExchange.getLocalAddress().getPort());
    }

    @Override
    public String getPath() {
        return notNull(httpExchange.getRequestURI().getPath());
    }

    @Override
    public String getQuery() {
        return notNull(httpExchange.getRequestURI().getQuery());
    }

    @Override
    public HttpRequest withBody() {
        state.updateAndGet(State::with);
        buffer();
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    public InputStream getInputStream() throws IOException {
        return buffer().getInputStream(httpExchange);
    }

    @Override
    public byte[] getBody() {
        return buffer().getBody();
    }

    @Override
    public String getProtocolVersion() {
        return httpExchange.getProtocol();
    }

    @Override
    public String getRequestUri() {
        return httpExchange.getRequestURI().toString();
    }

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(httpExchange)));
    }

    private String notNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
