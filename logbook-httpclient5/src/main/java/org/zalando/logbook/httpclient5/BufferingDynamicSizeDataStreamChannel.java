package org.zalando.logbook.httpclient5;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.nio.DataStreamChannel;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

final class BufferingDynamicSizeDataStreamChannel implements DataStreamChannel {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    byte[] getBuffer() {
        return buffer.toByteArray();
    }

    @Override
    public void requestOutput() {
    }

    @Override
    public int write(final ByteBuffer src) {
        return ByteBufferUtils.fixedSizeCopy(src, buffer);
    }

    @Override
    public void endStream() {
    }

    @Override
    public void endStream(final List<? extends Header> trailers) {
    }
}
