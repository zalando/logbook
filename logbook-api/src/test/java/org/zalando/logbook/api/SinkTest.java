package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SinkTest {

    private final Correlation correlation = mock(Correlation.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);

    private final Sink unit = mock(Sink.class);

    @Test
    void shouldBeActiveByDefault() {
        when(unit.isActive()).thenCallRealMethod();

        assertTrue(unit.isActive());
    }

    @Test
    void shouldDelegateToWriteRequestAndWriteResponseByDefault() throws IOException {
        doCallRealMethod().when(unit).writeBoth(any(), any(), any());

        unit.writeBoth(correlation, request, response);

        verify(unit).write(correlation, request);
        verify(unit).write(correlation, request, response);
    }

}
