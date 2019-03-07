package org.zalando.logbook.lle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;

public class LogbackLogstashLogWriterTest {

    @Test
    public void testDefaultImplementation() {
        LogbackLogstashLogWriter writer = new LogbackLogstashLogWriter() {

            @Override
            public void write(Precorrelation precorrelation, Marker request, String message) throws IOException {
            }

            @Override
            public void write(Correlation correlation, Marker response, String message) throws IOException {
            }
            
        };
        
        assertTrue(writer.isActive());
    }
}
