package org.zalando.logbook;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.function.Supplier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.zalando.logbook.LogbookFilter.RESPONSE_ATTRIBUTE_NAME;

final class DefaultTeeHttpServletResponse extends HttpServletResponseWrapper implements TeeHttpServletResponse {

    private final ByteArrayDataOutput content = ByteStreams.newDataOutput();
    private final Supplier<byte[]> supplier;

    DefaultTeeHttpServletResponse(final HttpServletResponse response, final Supplier<byte[]> supplier) {
        super(response);
        this.supplier = supplier;
    }

    private boolean isActive() {
        return supplier.get() == null;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new TeeServletOutputStream();
    }

    @Override
    public String getCharacterEncoding() {
        return firstNonNull(super.getCharacterEncoding(), "ISO-8859-1"); // TODO constant
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()), true);
    }

    @Override
    public byte[] getBodyAsByteArray() {
        if (isActive()) {
            return content.toByteArray();
        } else {
            return supplier.get();
        }
    }

    @Override
    public void finish() throws IOException {
        @Nullable final byte[] body = supplier.get();
        checkState(body != null, "Body is not available as attribute " + RESPONSE_ATTRIBUTE_NAME);

        final ServletResponse response = super.getResponse();

        if (!response.isCommitted()) {
            response.setContentLength(body.length);
        }

        getOriginal().write(body);
    }

    private ServletOutputStream getOriginal() throws IOException {
        return DefaultTeeHttpServletResponse.super.getOutputStream();
    }

    private final class TeeServletOutputStream extends ServletOutputStream {

        @Override
        public void write(final int b) throws IOException {
            if (isActive()) {
                content.write(b);
            } else {
                getOriginal().write(b);
            }
        }

        @Override
        public void write(final byte[] b) throws IOException {
            if (isActive()) {
                content.write(b);
            } else {
                getOriginal().write(b);
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            if (isActive()) {
                content.write(b, off, len);
            } else {
                getOriginal().write(b, off, len);
            }
        }

    }

}
