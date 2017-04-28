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

    private ServletOutputStream stream;
    private PrintWriter writer;
    private final String protocolVersion;

    /**
     * Null until we successfully intercepted it.
     */
    @Nullable
    private byte[] body;

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
        super(response);
        this.stream = new TeeServletOutputStream(output, super::getOutputStream);
        this.writer = new TeePrintWriter(stream, getCharset());
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
        this.stream = null;
        this.writer = null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            return super.getOutputStream();
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            return super.getWriter();
        }
        return writer;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            body = bytes.toByteArray();
        }
        return body;
    }

    private static final class TeeServletOutputStream extends ServletOutputStream {

        private final DataOutput output;
        private final LazyStream original;

        private TeeServletOutputStream(final DataOutput output,
                final LazyStream original) {
            this.output = output;
            this.original = original;
        }

        @Override
        public void write(final int b) throws IOException {
            output.write(b);
            original().write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            output.write(b, off, len);
            original().write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original().flush();
        }

        @Override
        public void close() throws IOException {
            original().close();
        }

        private OutputStream original() throws IOException {
            return original.retrieveLazily();
        }

    }

    @FunctionalInterface
    public interface LazyStream {
        ServletOutputStream retrieveLazily() throws IOException;
    }

    private static final class TeePrintWriter extends PrintWriter {

        TeePrintWriter(final ServletOutputStream stream, final Charset charset) {
            super(new OutputStreamWriter(stream, charset));
        }

    }

}
