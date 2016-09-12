package org.zalando.logbook.servlet;

import java.io.IOException;
import java.io.OutputStream;

final class NullOutputStream extends OutputStream {

    static final OutputStream NULL = new NullOutputStream();

    private NullOutputStream() {

    }

    @Override
    public void write(final int b) throws IOException {
        // ignore
    }

    @Override
    public void write(final byte[] b) throws IOException {
        // ignore
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        // ignore
    }

}
