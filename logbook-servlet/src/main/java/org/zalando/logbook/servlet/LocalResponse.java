package org.zalando.logbook.servlet;

import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.zalando.logbook.servlet.NullOutputStream.NULL;

final class LocalResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private DataOutput output = new DataOutputStream(bytes);

    private ServletOutputStream stream = new TeeServletOutputStream();
    private PrintWriter writer = new TeePrintWriter();
    private final String protocolVersion;

    /**
     * Null until we successfully intercepted it.
     */
    @Nullable
    private byte[] body;

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
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
    public void withoutBody() throws IOException {
        this.body = null;
        this.bytes = new ByteArrayOutputStream(0);
        this.output = new DataOutputStream(NULL);
        this.stream = super.getOutputStream();
        this.writer = super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            body = bytes.toByteArray();
        }
        return body;
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

    private final class TeePrintWriter extends PrintWriter {

        public TeePrintWriter() {
            super(new OutputStreamWriter(stream, getCharset()));
        }

    }

}
