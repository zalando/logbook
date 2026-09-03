package org.zalando.logbook.ecs;

import org.junit.jupiter.api.Test;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.StructuredHttpLogFormatter;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EcsSinkTest {

    final EcsSink unit = spy(EcsSink.class);
    final LoggingEventBuilder loggingEventBuilder = mock(LoggingEventBuilder.class);
    final StructuredHttpLogFormatter formatter = mock(StructuredHttpLogFormatter.class);

    @Test
    void writesContentUsingLoggingEventBuilder() throws IOException {
        String key = "http.version", value = "1.1";
        final Map<String, Object> content = Map.of(key, value);

        when(unit.getHttpLogFormatter()).thenReturn(formatter);
        when(formatter.format(content)).thenReturn(content.toString());
        when(loggingEventBuilder.addKeyValue(key, value)).thenReturn(loggingEventBuilder);

        unit.write(content, loggingEventBuilder);

        verify(loggingEventBuilder).addKeyValue(key, value);
        verify(loggingEventBuilder).log(content.toString());
    }
}