package org.zalando.logbook.jaxrs;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TeeOutputStreamTest {

    private final ByteArrayOutputStream output = spy(new ByteArrayOutputStream());
    private final TeeOutputStream unit1 = new TeeOutputStream(output);
    private final TeeOutputStream unit2 = new TeeOutputStream(null);

    @Test
    void shouldWriteByte() throws IOException {
        unit1.write(17);
        unit2.write(17);

        assertArrayEquals(unit1.toByteArray(), output.toByteArray());
        assertArrayEquals(unit2.toByteArray(), output.toByteArray());
    }

    @Test
    void shouldWriteBytesWithoutOffsets() throws IOException {
        unit1.write(new byte[]{17});
        unit2.write(new byte[]{17});

        assertArrayEquals(unit1.toByteArray(), output.toByteArray());
        assertArrayEquals(unit2.toByteArray(), output.toByteArray());

    }

    @Test
    void shouldWriteBytesWithOffsets() throws IOException {
        unit1.write(new byte[]{17}, 0, 1);
        unit2.write(new byte[]{17}, 0, 1);

        assertArrayEquals(unit1.toByteArray(), output.toByteArray());
        assertArrayEquals(unit2.toByteArray(), output.toByteArray());

    }

    @Test
    void shouldFlushAndClose() throws IOException {
        unit1.flush();
        verify(output, times(1)).flush();

        unit1.close();
        verify(output, times(1)).close();

        // This is just added to make coverage 100%
        unit2.flush();
        unit2.close();
    }

}
