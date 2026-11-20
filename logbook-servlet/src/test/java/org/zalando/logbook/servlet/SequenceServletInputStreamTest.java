package org.zalando.logbook.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class SequenceServletInputStreamTest {

    @Test
    void shouldReadSingleBytesAcrossStreams() throws IOException {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        final StringBuilder sb = new StringBuilder();
        int b;
        while ((b = unit.read()) != -1) {
            sb.append((char) b);
        }

        assertEquals("Hello World!", sb.toString());
    }

    @Test
    void shouldReadByteArray() throws IOException {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        final byte[] buffer = new byte[12];
        final int read = unit.read(buffer);

        assertEquals(5, read);
        final int readSecond = unit.read(buffer, 5, 7);
        assertEquals(7, readSecond);
        assertEquals("Hello World!", new String(buffer, UTF_8));
    }

    @Test
    void shouldHandleReadBoundsCheck() {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        assertThrows(IndexOutOfBoundsException.class, () -> unit.read(new byte[5], -1, 2));
        assertThrows(IndexOutOfBoundsException.class, () -> unit.read(new byte[5], 0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> unit.read(new byte[5], 2, 4));
    }

    @Test
    void shouldReturnZeroWhenReadingZeroLength() throws IOException {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        assertEquals(0, unit.read(new byte[5], 0, 0));
    }

    @Test
    void shouldSkipBytesAcrossStreams() throws IOException {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        assertEquals(0, unit.skip(-1));
        assertEquals(0, unit.skip(0));

        assertEquals(2, unit.skip(2));
        assertEquals('l', unit.read());

        assertEquals(7, unit.skip(7));
        assertEquals('d', unit.read());

        assertEquals(1, unit.skip(1));
        assertEquals(-1, unit.read());
    }

    @Test
    void shouldReportAvailable() throws IOException {
        final byte[] prefix = "Hello".getBytes(UTF_8);
        final ServletInputStream second = new ServletInputStreamAdapter(
                new ByteArrayInputStream(" World!".getBytes(UTF_8)));
        final SequenceServletInputStream unit = new SequenceServletInputStream(prefix, second);

        assertEquals(12, unit.available());
    }

    @Test
    void shouldCloseBothStreams() throws IOException {
        final ServletInputStream second = mock(ServletInputStream.class);
        final SequenceServletInputStream unit = new SequenceServletInputStream(new byte[0], second);

        unit.close();

        verify(second).close();
    }

    @Test
    void shouldReportIsFinishedCorrectly() throws IOException {
        final ServletInputStream second = mock(ServletInputStream.class);
        when(second.isFinished()).thenReturn(false, true);

        final SequenceServletInputStream unit = new SequenceServletInputStream("A".getBytes(UTF_8), second);

        assertFalse(unit.isFinished());

        unit.read();

        assertFalse(unit.isFinished());
        assertTrue(unit.isFinished());
    }

    @Test
    void shouldReportIsReadyCorrectly() throws IOException {
        final ServletInputStream second = mock(ServletInputStream.class);
        when(second.isReady()).thenReturn(false, true);

        final SequenceServletInputStream unit = new SequenceServletInputStream("A".getBytes(UTF_8), second);

        assertTrue(unit.isReady());

        unit.read();

        assertFalse(unit.isReady());
        assertTrue(unit.isReady());
    }

    @Test
    void shouldNotSupportReadListener() {
        final ServletInputStream second = mock(ServletInputStream.class);
        final SequenceServletInputStream unit = new SequenceServletInputStream(new byte[0], second);

        assertThrows(UnsupportedOperationException.class, () ->
                unit.setReadListener(mock(ReadListener.class)));
    }

}
