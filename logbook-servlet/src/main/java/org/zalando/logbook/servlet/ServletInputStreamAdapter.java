package org.zalando.logbook.servlet;

import lombok.AllArgsConstructor;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@AllArgsConstructor
final class ServletInputStreamAdapter extends ServletInputStream {

    private final ByteArrayInputStream stream;

    @Override
    public int read() {
        return stream.read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) {
        return stream.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        return stream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(final ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

}
