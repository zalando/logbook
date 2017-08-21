package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

final class LocalResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private final String protocolVersion;

    private boolean withBody;
    private Tee tee;

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
        super(response);
        this.protocolVersion = protocolVersion;
        this.withBody = true;
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
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();

        for (final String header : getHeaderNames()) {
            builder.put(header, getHeaders(header));
        }

        return builder.build();
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        assertWithBody();
        return this;
    }

    @Override
    public void withoutBody() throws IOException {
        assertWithBody();
        withBody = false;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (withBody) {
            return tee().getOutputStream();
        } else {
            return super.getOutputStream();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (withBody) {
            return tee().getWriter(this::getCharset);
        } else {
            return super.getWriter();
        }
    }

    @Override
    public byte[] getBody() throws IOException {
        assertWithBody();
        return tee().getBytes();
    }

    private void assertWithBody() {
        if (!withBody) {
            throw new IllegalStateException("Response is without body");
        }
    }

    private Tee tee() throws IOException {
        if (tee == null) {
            tee = new Tee(super.getOutputStream());
        }
        return tee;
    }

    private static class Tee {

        private final ByteArrayOutputStream branch;
        private final TeeServletOutputStream output;

        private PrintWriter writer;
        private byte[] bytes;

        private Tee(final OutputStream original) throws IOException {
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

        byte[] getBytes() {
            if (bytes == null || bytes.length != branch.size()) {
                bytes = branch.toByteArray();
            }
            return bytes;
        }
    }

    private static class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;
        private final OutputStream branch;

        private TeeServletOutputStream(final OutputStream original, final OutputStream branch) {
            this.original = original;
            this.branch = branch;
        }

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
