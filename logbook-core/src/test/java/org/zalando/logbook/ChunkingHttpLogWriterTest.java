package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ChunkingHttpLogWriterTest {

    private final HttpLogWriter delegate = mock(HttpLogWriter.class);
    private final HttpLogWriter unit = new ChunkingHttpLogWriter(20, delegate);

    @Test
    void shouldDelegateActive() {
        assertThat(unit.isActive(), is(false));
    }

    @Test
    void shouldWriteSingleRequestIfLengthNotExceeded() throws IOException {
        final List<String> precorrelation = captureRequest("HelloWorld");
        assertThat(precorrelation, contains("HelloWorld"));
    }

    @Test
    void shouldWriteRequestInChunksIfLengthExceeded() throws IOException {
        final List<String> precorrelation = captureRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        assertThat(precorrelation,
                contains("Lorem ipsum dolor ", "sit amet, ", "consectetur ", "adipiscing elit"));
    }

    private List<String> captureRequest(final String request) throws IOException {
        unit.write(new SimplePrecorrelation(Clock.systemUTC()), request);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(delegate, atLeastOnce()).write(any(Precorrelation.class), captor.capture());
        return captor.getAllValues();
    }

    @Test
    void shouldWriteSingleResponseIfLengthNotExceeded() throws IOException {
        final List<String> precorrelation = captureResponse("Hello");
        assertThat(precorrelation, contains("Hello"));

    }

    @Test
    void shouldWriteResponseInChunksIfLengthExceeded() throws IOException {
        final List<String> precorrelation = captureResponse("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        assertThat(precorrelation,
                contains("Lorem ipsum dolor ", "sit amet, ", "consectetur ", "adipiscing elit"));
    }

    @Test
    void shouldFailOnInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new ChunkingHttpLogWriter(0, delegate));
    }

    @Test
    void shouldCreateWithSizeOfOne() {
        new ChunkingHttpLogWriter(1, delegate);
    }

    private List<String> captureResponse(final String response) throws IOException {
        unit.write(new SimpleCorrelation("id", ZERO), response);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(delegate, atLeastOnce()).write(any(), captor.capture());
        return captor.getAllValues();
    }
}
