package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.PROTECTED;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

final class Response implements HttpResponse {

    private final HttpExchange httpExchange;

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

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

        default OutputStream getOutputStream(final HttpExchange exchange) throws IOException {
            return exchange.getResponseBody();
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
        public State buffer(final HttpExchange exchange) throws IOException {
            final Tee tee = new Tee(exchange.getResponseBody());
            return new Buffering(tee);
        }

    }

    @AllArgsConstructor
    private static abstract class Streaming implements State {

        @Getter(PROTECTED)
        private final Tee tee;

        @Override
        public OutputStream getOutputStream(final HttpExchange exchange) {
            return tee.getOutputStream();
        }

    }

    private static final class Buffering extends Streaming {

        Buffering(final Tee tee) {
            super(tee);
        }

        @Override
        public State without() {
            return new Ignoring(getTee());
        }

        @Override
        public byte[] getBody() {
            return getTee().getBytes();
        }

    }

    private static final class Ignoring extends Streaming {

        Ignoring(final Tee tee) {
            super(tee);
        }

        @Override
        public State with() {
            return new Buffering(getTee());
        }

    }

    public Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    public OutputStream getOutputStream() throws IOException {
        return buffer().getOutputStream(httpExchange);
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();
        for (Map.Entry<String, List<String>> e : httpExchange.getResponseHeaders().entrySet()) {
            headers = headers.update(e.getKey(), e.getValue());
        }
        return headers;
    }

    @Override
    public byte[] getBody() throws IOException {
        return buffer().getBody();
    }

    @Override
    public int getStatus() {
        return httpExchange.getResponseCode();
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

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(httpExchange)));
    }

    private static class Tee {

        private final ByteArrayOutputStream branch;
        private final TeeOutputStream output;

        private byte[] bytes;

        private Tee(final OutputStream original) {
            this.branch = new ByteArrayOutputStream();
            this.output = new TeeOutputStream(original, branch);
        }

        OutputStream getOutputStream() {
            return output;
        }

        byte[] getBytes() {
            if (bytes == null) {
                bytes = branch.toByteArray();
            }
            return bytes;
        }
    }

    @AllArgsConstructor
    private static class TeeOutputStream extends OutputStream {

        private final OutputStream original;
        private final OutputStream branch;

        @Override
        public void write(final int b) throws IOException {
            original.write(b);
            branch.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            original.write(b, off, len);
            branch.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
            branch.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
            branch.close();
        }

    }
}

