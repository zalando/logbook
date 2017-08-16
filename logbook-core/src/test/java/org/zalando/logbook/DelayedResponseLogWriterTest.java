package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class DelayedResponseLogWriterTest {

    private final HttpLogWriter delegate = Mockito.mock(HttpLogWriter.class);
    private final HttpLogWriter unit = new DelayedResponseLogWriter(delegate);

    @Test
    void shouldDelayRequestLogging() throws IOException {
        final Logbook logbook = Logbook.builder().writer(unit).build();

        final Correlator correlator = logbook.write(MockRawHttpRequest.create()).orElseThrow(AssertionError::new);

        verify(delegate, never()).writeRequest(any());

        correlator.write(MockRawHttpResponse.create().withStatus(404));

        verify(delegate).writeRequest(any());
        verify(delegate).writeResponse(any());
    }

}
