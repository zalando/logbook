package org.zalando.logbook.servlet;

import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.servlet.junit.RestoreSystemProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NotThreadSafe
@RestoreSystemProperties
class MaxBufferSizeTest {

    @Test
    void defaultsToMinusOne() {
        System.clearProperty("logbook.servlet.max-buffer-size");
        System.clearProperty("logbook.filter.max-buffer-size");
        assertEquals(-1L, LogbookFilter.maxBufferSizeFromProperties());
    }

    @Test
    void readsServletProperty() {
        System.setProperty("logbook.servlet.max-buffer-size", "1024");
        assertEquals(1024L, LogbookFilter.maxBufferSizeFromProperties());
    }

    @Test
    void readsFilterPropertyFallback() {
        System.clearProperty("logbook.servlet.max-buffer-size");
        System.setProperty("logbook.filter.max-buffer-size", "2048");
        assertEquals(2048L, LogbookFilter.maxBufferSizeFromProperties());
    }

    @Test
    void trimsWhitespaceFromProperty() {
        System.setProperty("logbook.servlet.max-buffer-size", " 4096 \t");
        assertEquals(4096L, LogbookFilter.maxBufferSizeFromProperties());
    }

}
