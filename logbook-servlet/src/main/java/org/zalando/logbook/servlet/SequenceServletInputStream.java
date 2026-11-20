package org.zalando.logbook.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

final class SequenceServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream first;
    private final ServletInputStream second;

    SequenceServletInputStream(final byte[] prefix, final ServletInputStream second) {
        this.first = new ByteArrayInputStream(Objects.requireNonNull(prefix));
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public int read() throws IOException {
        if (first.available() > 0) {
            return first.read();
        }
        return second.read();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        Objects.requireNonNull(b);
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (first.available() > 0) {
            return first.read(b, off, len);
        }
        return second.read(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        if (first.available() > 0) {
            final long skipped = first.skip(n);
            if (skipped < n) {
                return skipped + second.skip(n - skipped);
            }
            return skipped;
        }
        return second.skip(n);
    }

    @Override
    public int available() throws IOException {
        return first.available() + second.available();
    }

    @Override
    public void close() throws IOException {
        try {
            first.close();
        } finally {
            second.close();
        }
    }

    @Override
    public boolean isFinished() {
        return first.available() == 0 && second.isFinished();
    }

    @Override
    public boolean isReady() {
        return first.available() > 0 || second.isReady();
    }

    @Override
    public void setReadListener(final ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

}
