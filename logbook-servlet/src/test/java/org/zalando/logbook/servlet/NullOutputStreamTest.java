package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import static org.zalando.logbook.servlet.NullOutputStream.NULL;

public final class NullOutputStreamTest {

    @Test
    void shouldIgnoreByte() throws Exception {
        NULL.write(0);
    }

    @Test
    void shouldIgnoreBytes() throws Exception {
        NULL.write(new byte[0]);
    }

    @Test
    void shouldIgnoreBytesOffset() throws Exception {
        NULL.write(new byte[0], 0, 0);
    }

}
