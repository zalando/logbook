package org.zalando.logbook.openfeign;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UtilsTest {

    @Test
    void ensureClosedShouldIgnoreCloseIoException() throws IOException {
        Closeable closeable = mock(Closeable.class);
        doThrow(new IOException()).when(closeable).close();

        Utils.ensureClosed(closeable);
    }

    @Test
    void ensureClosedShouldIgnoreNull() {
        Utils.ensureClosed(null);
    }
}