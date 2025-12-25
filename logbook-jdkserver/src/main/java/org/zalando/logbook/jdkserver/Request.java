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
