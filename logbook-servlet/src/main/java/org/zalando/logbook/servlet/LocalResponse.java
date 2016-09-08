package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
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

import static java.nio.charset.StandardCharsets.UTF_8;

final class LocalResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final TeeServletOutputStream stream;
    private final PrintWriter writer;
    private final String protocolVersion;

    /**
     * Null until we successfully intercepted it.
     */
    @Nullable
    private byte[] body;

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
        super(response);
        this.stream = new TeeServletOutputStream();
        this.writer = new TeePrintWriter();
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
    public Map<String, List<String>> getHeaders() {
        final HeadersBuilder builder = new HeadersBuilder();

        for (final String header : getHeaderNames()) {
            builder.put(header, getHeaders(header));
        }

        return builder.build();
    }

    @Override
    public String getContentType() {
        return Optional.ofNullable(super.getContentType()).orElse("");
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        return this;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // TODO stop intercepting when we know we don't want it
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            body = output.toByteArray();
        }
        return body;
    }

    @Override
    public String getBodyAsString() throws IOException {
        return output.toString(getCharset().name());
    }

    ByteArrayOutputStream getOutput() {
        return output;
    }

    private final class TeePrintWriter extends PrintWriter {

        public TeePrintWriter() {
            super(new OutputStreamWriter(stream, getCharset()));
        }

    }

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;

        private TeeServletOutputStream() throws IOException {
            this.original = LocalResponse.super.getOutputStream();
        }

        @Override
        public void write(final int b) throws IOException {
            output.write(b);
            original.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            output.write(b, off, len);
            original.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
        }

    }

}
