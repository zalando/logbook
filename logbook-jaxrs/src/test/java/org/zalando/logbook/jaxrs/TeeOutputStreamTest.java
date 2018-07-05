package org.zalando.logbook.jaxrs;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TeeOutputStreamTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final TeeOutputStream unit = new TeeOutputStream(output);

    @Test
    public void shouldWriteByte() throws IOException {
        unit.write(17);

        assertArrayEquals(unit.toByteArray(), output.toByteArray());
    }

    @Test
    public void shouldWriteBytesWithoutOffsets() throws IOException {
        unit.write(new byte[] {17});

        assertArrayEquals(unit.toByteArray(), output.toByteArray());

    }

    @Test
    public void shouldWriteBytesWithOffsets() throws IOException {
        unit.write(new byte[] {17}, 0, 1);

        assertArrayEquals(unit.toByteArray(), output.toByteArray());

    }

}
