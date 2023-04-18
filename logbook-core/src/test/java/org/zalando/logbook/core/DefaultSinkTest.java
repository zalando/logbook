package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogFormatter;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.api.Sink;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultSinkTest {

    private final HttpLogFormatter formatter = mock(HttpLogFormatter.class);
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Sink unit = new DefaultSink(formatter, writer);

    private final Precorrelation precorrelation = mock(Precorrelation.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final Correlation correlation = mock(Correlation.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Test
    void isActiveIfWriterIsActive() {
        when(writer.isActive()).thenReturn(true);

        assertTrue(unit.isActive());
    }

    @Test
    void isInactiveIfWriterIsInactive() {
        when(writer.isActive()).thenReturn(false);

        assertFalse(unit.isActive());
    }

    @Test
    void writeRequest() throws IOException {
        when(formatter.format(precorrelation, request)).thenReturn("request");
        unit.write(precorrelation, request);
        verify(writer).write(precorrelation, "request");
    }

    @Test
    void writeResponse() throws IOException {
        when(formatter.format(correlation, response)).thenReturn("response");
        unit.write(correlation, request, response);
        verify(writer).write(correlation, "response");
    }

    @Test
    void writeBoth() throws IOException {
        when(formatter.format(correlation, request)).thenReturn("request");
        when(formatter.format(correlation, response)).thenReturn("response");
        unit.writeBoth(correlation, request, response);
        verify(writer).write((Precorrelation) correlation, "request");
        verify(writer).write(correlation, "response");
    }

}
