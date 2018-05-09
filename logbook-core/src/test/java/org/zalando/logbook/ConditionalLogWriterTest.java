package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.zalando.logbook.Conditions.statusAtLeast;

public final class ConditionalLogWriterTest {

    private final HttpLogWriter delegate = Mockito.mock(HttpLogWriter.class);
    private final HttpLogWriter unit = new ConditionalLogWriter(statusAtLeast(400), delegate);

    @Test
    void shouldSkip() throws IOException {
        final Logbook logbook = Logbook.builder().writer(unit).build();

        final Correlator correlator = logbook.write(MockRawHttpRequest.create()).orElseThrow(AssertionError::new);
        correlator.write(MockRawHttpResponse.create());

        verify(delegate, never()).writeRequest(any());
        verify(delegate, never()).writeResponse(any());
    }

    @Test
    void shouldLog() throws IOException {
        final Logbook logbook = Logbook.builder().writer(unit).build();

        final Correlator correlator = logbook.write(MockRawHttpRequest.create()).orElseThrow(AssertionError::new);

        verify(delegate, never()).writeRequest(any());

        correlator.write(MockRawHttpResponse.create().withStatus(404));

        verify(delegate).writeRequest(any());
        verify(delegate).writeResponse(any());
    }

}
