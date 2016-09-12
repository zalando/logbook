package org.zalando.logbook.servlet;

import org.junit.Test;

import static org.zalando.logbook.servlet.NullOutputStream.NULL;

public final class NullOutputStreamTest {

    @Test
    public void shouldIgnoreByte() throws Exception {
        NULL.write(0);
    }

    @Test
    public void shouldIgnoreBytes() throws Exception {
        NULL.write(new byte[0]);
    }

    @Test
    public void shouldIgnoreBytesOffset() throws Exception {
        NULL.write(new byte[0], 0, 0);
    }

}