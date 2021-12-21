package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LogbookFilterTest {

    @Test
    public void shouldReturnDescription() {
        assertEquals("Logbook filter", new LogbookFilter().description());
    }

    @Test
    public void shouldDoRequestAndResponseProcessing() {

    }

}
