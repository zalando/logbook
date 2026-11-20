package org.zalando.logbook.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

final class ByteStreams {

    private ByteStreams() {

    }

    static byte[] toByteArray(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    static byte[] toByteArray(final InputStream in, final long maxBytes) throws IOException {
        if (maxBytes < 0 || maxBytes == Long.MAX_VALUE) {
            return toByteArray(in);
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        long totalRead = 0;
        int bytesRead;

        while (totalRead <= maxBytes && (bytesRead = in.read(buf, 0, (int) Math.min(buf.length, maxBytes + 1 - totalRead))) != -1) {
            out.write(buf, 0, bytesRead);
            totalRead += bytesRead;
        }
        return out.toByteArray();
    }

    static void copy(final InputStream from, final OutputStream to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        final byte[] buf = new byte[4096];
        int bytesRead;

        while ((bytesRead = from.read(buf)) != -1) {
            to.write(buf, 0, bytesRead);
        }
    }

}
