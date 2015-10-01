package org.zalando.logbook.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.RawHttpResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

final class TeeResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse, Closeable {

    private final HttpServletRequest request;
    private final ByteArrayDataOutput output = ByteStreams.newDataOutput();

    /**
     * Null until we a) capture it ourselves or b) retrieve it from {@link Attributes#RESPONSE_BODY}, which
     * was previously captured by another filter instance.
     */
    private byte[] body;

    TeeResponse(final HttpServletRequest request, final HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    @Nullable
    private byte[] getAlreadyBufferedResponseBody() {
        return (byte[]) request.getAttribute(Attributes.RESPONSE_BODY);
    }

    private boolean isAlreadyBuffered() {
        return getAlreadyBufferedResponseBody() != null;
    }

    private boolean isCurrentlyBeingBufferedByNobodyOrUs() {
        return isCurrentlyBeingBufferedBy(null) || isCurrentlyBeingBufferedBy(this);
    }

    private boolean isCurrentlyBeingBufferedBy(@Nullable final Object buffer) {
        return request.getAttribute(Attributes.BUFFERING) == buffer;
    }

    @Override
    public Multimap<String, String> getHeaders() {
        final Multimap<String, String> headers = ArrayListMultimap.create();

        for (final String header : getHeaderNames()) {
            headers.putAll(header, getHeaders(header));
        }

        return headers;
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getCharacterEncoding()).map(Charset::forName).orElse(ISO_8859_1);
    }

    @Override
    public void close() throws IOException {
        if (body == null) {
            return;
        }

        if (!isCommitted()) {
            setContentLength(body.length);
        }

        super.getOutputStream().write(body);
    }

    @Override
    public HttpResponse withBody() {
        if (body != null) {
            // somebody called the method twice
            return this;
        }

        if (isAlreadyBuffered()) {
            body = getAlreadyBufferedResponseBody();
        } else if (isCurrentlyBeingBufferedByNobodyOrUs()) {
            body = output.toByteArray();
        } else {
            throw new IllegalStateException("Body wasn't buffered before, but neither by us?!");
        }

        request.setAttribute(Attributes.RESPONSE_BODY, body);
        return this;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return body == null ?
                new TeeServletOutputStream() :
                super.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharset()));
    }

    @Override
    public byte[] getBody() {
        Preconditions.checkState(body != null, "Body was not read before");
        return body;
    }

    @VisibleForTesting
    ByteArrayDataOutput getOutput() {
        return output;
    }

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;

        private TeeServletOutputStream() throws IOException {
            this.original = TeeResponse.super.getOutputStream();
        }

        @Override
        public void write(final int b) throws IOException {
            if (isCurrentlyBeingBufferedByNobodyOrUs()) {
                request.setAttribute(Attributes.BUFFERING, TeeResponse.this);
                output.write(b);
            } else {
                original.write(b);
            }
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (isCurrentlyBeingBufferedByNobodyOrUs()) {
                request.setAttribute(Attributes.BUFFERING, TeeResponse.this);
                output.write(b);
            } else {
                original.write(b);
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            if (isCurrentlyBeingBufferedByNobodyOrUs()) {
                request.setAttribute(Attributes.BUFFERING, TeeResponse.this);
                output.write(b, off, len);
            } else {
                original.write(b, off, len);
            }
        }

    }

}
