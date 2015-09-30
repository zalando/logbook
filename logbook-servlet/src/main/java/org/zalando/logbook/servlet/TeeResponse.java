package org.zalando.logbook.servlet;

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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

final class TeeResponse extends HttpServletResponseWrapper implements RawHttpResponse, HttpResponse {

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
    private byte[] getPrevious() {
        return (byte[]) request.getAttribute(Attributes.RESPONSE_BODY);
    }

    private boolean isActive() {
        return getPrevious() == null;
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
    public HttpResponse withBody() {
        if (body != null) {
            return this;
        }

        if (isActive()) {
            this.body = output.toByteArray();
        } else {
            this.body = getPrevious();
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

    private final class TeeServletOutputStream extends ServletOutputStream {

        private final OutputStream original;

        private TeeServletOutputStream() throws IOException {
            this.original = TeeResponse.super.getOutputStream();
        }

        @Override
        public void write(final int b) throws IOException {
            original.write(b);

            if (isActive()) {
                output.write(b);
            }
        }

        @Override
        public void write(final byte[] b) throws IOException {
            original.write(b);

            if (isActive()) {
                output.write(b);
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            original.write(b, off, len);

            if (isActive()) {
                output.write(b, off, len);
            }
        }

    }

}
