package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompositeSinkTest {

    private final Sink first = mock(Sink.class);
    private final Sink second = mock(Sink.class);
    private final Sink unit = new CompositeSink(Arrays.asList(first, second));

    private final Precorrelation precorrelation = mock(Precorrelation.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final Correlation correlation = mock(Correlation.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Test
    void isActiveIfAny() {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(false);

        assertTrue(unit.isActive());
    }

    @Test
    void isActiveIfAll() {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(true);

        assertTrue(unit.isActive());
    }

    @Test
    void isInactiveIfNone() {
        when(first.isActive()).thenReturn(false);
        when(second.isActive()).thenReturn(false);

        assertFalse(unit.isActive());
    }

    @Test
    void writeRequestToAll() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(true);

        unit.write(precorrelation, request);

        verify(first).write(precorrelation, request);
        verify(second).write(precorrelation, request);
    }

    @Test
    void writeResponseToAll() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(true);

        unit.write(correlation, request, response);

        verify(first).write(correlation, request, response);
        verify(second).write(correlation, request, response);
    }

    @Test
    void writeBothToAll() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(true);

        unit.writeBoth(correlation, request, response);

        verify(first).writeBoth(correlation, request, response);
        verify(second).writeBoth(correlation, request, response);
    }

    @Test
    void writeRequestToActive() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(false);

        unit.write(precorrelation, request);

        verify(first).write(precorrelation, request);
        verify(second, never()).write(precorrelation, request);
    }

    @Test
    void writeResponseToActive() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(false);

        unit.write(correlation, request, response);

        verify(first).write(correlation, request, response);
        verify(second, never()).write(correlation, request, response);
    }

    @Test
    void writeBothToActive() throws IOException {
        when(first.isActive()).thenReturn(true);
        when(second.isActive()).thenReturn(false);

        unit.writeBoth(correlation, request, response);

        verify(first).writeBoth(correlation, request, response);
        verify(second, never()).writeBoth(correlation, request, response);
    }

}
