package org.zalando.logbook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ChunkingHttpLogWriterTest {

    private final HttpLogWriter delegate = mock(HttpLogWriter.class);
    private final HttpLogWriter unit = new ChunkingHttpLogWriter(20, delegate);

    @Captor
    private ArgumentCaptor<Precorrelation<String>> requestCaptor;

    @Captor
    private ArgumentCaptor<Correlation<String, String>> responseCaptor;

    @Test
    public void shouldDelegateActive() throws IOException {
        final RawHttpRequest request = mock(RawHttpRequest.class);
        assertThat(unit.isActive(request), is(false));
    }

    @Test
    public void shouldWriteSingleRequestIfLengthNotExceeded() throws IOException {
        final List<String> precorrelation = captureRequest("HelloWorld");
        assertThat(precorrelation, contains("HelloWorld"));
    }

    @Test
    public void shouldWriteRequestInChunksIfLengthExceeded() throws IOException {
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
    public void shouldWriteSingleResponseIfLengthNotExceeded() throws IOException {
        final List<String> precorrelation = captureResponse("Hello");
        assertThat(precorrelation, contains("Hello"));

    }

    @Test
    public void shouldWriteResponseInChunksIfLengthExceeded() throws IOException {
        final List<String> precorrelation = captureResponse("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        assertThat(precorrelation,
                contains("Lorem ipsum dolor ", "sit amet, ", "consectetur ", "adipiscing elit"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnInvalidSize() throws IOException {
        new ChunkingHttpLogWriter(0, delegate);
    }

    @Test
    public void shouldCreateWithSizeOfOne() throws IOException {
        new ChunkingHttpLogWriter(1, delegate);
    }

    private List<String> captureResponse(final String response) throws IOException {
        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("id", ZERO, "", response));

        verify(delegate, atLeastOnce()).writeResponse(responseCaptor.capture());

        return responseCaptor.getAllValues().stream()
                .map(Correlation::getResponse)
                .collect(toList());
    }
}
