package org.zalando.logbook.servlet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PROTECTED;
import static org.zalando.fauxpas.FauxPas.throwingUnaryOperator;

final class LocalResponse extends HttpServletResponseWrapper implements HttpResponse {

    private final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());

    private final String protocolVersion;

    private interface State {

        default State with() {
            return this;
        }

        default State without() {
            return this;
        }

        default State buffer(
                final ServletResponse response) throws IOException {

            return this;
        }

        default ServletOutputStream getOutputStream(
                final ServletResponse response) throws IOException {

            return response.getOutputStream();
        }

        default PrintWriter getWriter(
                final ServletResponse response,
                final Supplier<Charset> charset) throws IOException {

            return response.getWriter();
        }

        default void flush() throws IOException {
            // nothing to do here
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
        public State buffer(final ServletResponse response) throws IOException {
            final Tee tee = new Tee(response.getOutputStream());
            return new Buffering(tee);
        }

    }

    @AllArgsConstructor
    private static abstract class Streaming implements State {

        @Getter(PROTECTED)
        private final Tee tee;

        @Override
        public ServletOutputStream getOutputStream(final ServletResponse response) {
            return tee.getOutputStream();
        }

        @Override
        public PrintWriter getWriter(final ServletResponse response, final Supplier<Charset> charset) {
            return tee.getWriter(charset);
        }

        @Override
        public void flush() throws IOException {
            tee.flush();
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

    LocalResponse(final HttpServletResponse response, final String protocolVersion) {
        super(response);
        this.protocolVersion = protocolVersion;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = HttpHeaders.empty();

        for (final String header : getHeaderNames()) {
            headers = headers.update(header, getHeaders(header));
        }

        return headers;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
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

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return buffer().getOutputStream(getResponse());
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return buffer().getWriter(getResponse(), this::getCharset);
    }

    @Override
    public void flushBuffer() throws IOException {
        state.get().flush();
        super.flushBuffer();
    }

    @Override
    public byte[] getBody() {
        return buffer().getBody();
    }

    private State buffer() {
        return state.updateAndGet(throwingUnaryOperator(state ->
                state.buffer(getResponse())));
    }

    private static class Tee {

        private final ByteArrayOutputStream branch;
        private final TeeServletOutputStream output;

        private PrintWriter writer;
        private byte[] bytes;

        private Tee(final ServletOutputStream original) {
            this.branch = new ByteArrayOutputStream();
            this.output = new TeeServletOutputStream(original, branch);
        }

        ServletOutputStream getOutputStream() {
            return output;
        }

        PrintWriter getWriter(final Supplier<Charset> charset) {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(output, charset.get()));
            }
            return writer;
        }

        void flush() throws IOException {
            if (writer == null) {
                output.flush();
            } else {
                writer.flush();
            }
        }

        byte[] getBytes() {
            if (bytes == null) {
                bytes = branch.toByteArray();
            }
            return bytes;
        }
    }

    @AllArgsConstructor
    private static class TeeServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream original;
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

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setWriteListener(final WriteListener listener) {
            original.setWriteListener(listener);
        }

    }
}
