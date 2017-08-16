package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ChunkingHttpLogWriterTest {

    private final HttpLogWriter delegate = mock(HttpLogWriter.class);
    private final HttpLogWriter unit = new ChunkingHttpLogWriter(20, delegate);

    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<Precorrelation<String>> requestCaptor = forClass(Precorrelation.class);

    @SuppressWarnings("unchecked")
    private final ArgumentCaptor<Correlation<String, String>> responseCaptor = forClass(Correlation.class);

    @Test
    void shouldDelegateActive() throws IOException {
        final RawHttpRequest request = mock(RawHttpRequest.class);
        assertThat(unit.isActive(request), is(false));
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
        unit.writeRequest(new SimplePrecorrelation<>("id", request));

        verify(delegate, atLeastOnce()).writeRequest(requestCaptor.capture());

        return requestCaptor.getAllValues().stream()
                .map(Precorrelation::getRequest)
                .collect(toList());
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
    void shouldFailOnInvalidSize() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> new ChunkingHttpLogWriter(0, delegate));
    }

    @Test
    void shouldCreateWithSizeOfOne() throws IOException {
        new ChunkingHttpLogWriter(1, delegate);
    }

    private List<String> captureResponse(final String response) throws IOException {
        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("id", ZERO, "", response,
                MockHttpRequest.create(), MockHttpResponse.create()));

        verify(delegate, atLeastOnce()).writeResponse(responseCaptor.capture());

        return responseCaptor.getAllValues().stream()
                .map(Correlation::getResponse)
                .collect(toList());
    }
}
