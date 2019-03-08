package org.zalando.logbook.lle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.StructuredHttpLogFormatter;

class LogstashLogbackSinkTest {

    private final StructuredHttpLogFormatter formatter = mock(StructuredHttpLogFormatter.class);
    private final LogstashLogbackHttpLogWriter writer = mock(LogstashLogbackHttpLogWriter.class);
    private final LogstashLogbackSink unit = new LogstashLogbackSink(formatter, writer);

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
        when(request.getScheme()).thenReturn("https");
        when(request.getMethod()).thenReturn("method");
        when(request.getRequestUri()).thenReturn("requestUri");
        unit.write(precorrelation, request);
        verify(writer).write(precorrelation, new AutodetectPrettyPrintingMarker("http", "request"), "method requestUri");
    }

    @Test
    void writeResponse() throws IOException {
        when(formatter.format(correlation, response)).thenReturn("response");
        when(request.getScheme()).thenReturn("https");
        when(request.getMethod()).thenReturn("method");
        when(request.getRequestUri()).thenReturn("requestUri");
        when(response.getStatus()).thenReturn(200);
        unit.write(correlation, request, response);
        verify(writer).write(correlation, new AutodetectPrettyPrintingMarker("http", "response"), "method requestUri 200");
    }

    @Test
    void writeBoth() throws IOException {
        when(formatter.format(correlation, request)).thenReturn("request");
        when(formatter.format(correlation, response)).thenReturn("response");
        when(request.getScheme()).thenReturn("https");
        when(request.getMethod()).thenReturn("method");
        when(request.getRequestUri()).thenReturn("requestUri");
        when(response.getStatus()).thenReturn(200);
        unit.writeBoth(correlation, request, response);
        verify(writer).write((Precorrelation) correlation, new AutodetectPrettyPrintingMarker("http", "request"), "method requestUri");
        verify(writer).write(correlation, new AutodetectPrettyPrintingMarker("http", "response"), "method requestUri 200");
    }

}
