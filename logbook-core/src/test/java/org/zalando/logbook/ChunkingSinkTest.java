package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import static java.time.Instant.MIN;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

final class ChunkingSinkTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Sink delegate = new DefaultSink(new DefaultHttpLogFormatter(), writer);
    private final Sink unit = new ChunkingSink(delegate, 20);

    @Test
    void delegatesActive() {
        assertThat(unit.isActive()).isFalse();
    }

    @Test
    void ignoresEmptyBodies() throws IOException {
        final List<String> chunks = captureRequest("");

        assertThat(chunks)
                .allSatisfy(chunk -> assertThat(chunk)
                        .startsWith("Incoming Request"));
    }

    @Test
    void shouldWriteSingleRequestIfLengthNotExceeded() throws IOException {
        final List<String> chunks = captureRequest("HelloWorld");

        assertThat(chunks)
                .allSatisfy(chunk -> assertThat(chunk)
                        .startsWith("Incoming Request")
                        .endsWith("HelloWorld"));
    }

    @Test
    void shouldWriteRequestInChunksIfLengthExceeded() throws IOException {
        final List<String> chunks = captureRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit");

        assertThat(chunks)
                .zipSatisfy(
                        Arrays.asList("Lorem ipsum dolor ", "sit amet, ", "consectetur ", "adipiscing elit"),
                        (chunk, end) -> assertThat(chunk)
                                .startsWith("Incoming Request")
                                .endsWith(end));
    }

    private List<String> captureRequest(final String request) throws IOException {
        unit.write(new SimplePrecorrelation(() -> "", Clock.systemUTC()), MockHttpRequest.create().withBodyAsString(request));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, atLeastOnce()).write(any(Precorrelation.class), captor.capture());
        return captor.getAllValues();
    }

    @Test
    void shouldWriteSingleResponseIfLengthNotExceeded() throws IOException {
        final List<String> chunks = captureResponse("HelloWorld");

        assertThat(chunks).singleElement(as(STRING))
                .startsWith("Outgoing Response")
                .endsWith("HelloWorld");

    }

    @Test
    void shouldWriteResponseInChunksIfLengthExceeded() throws IOException {
        final List<String> chunks = captureResponse("Lorem ipsum dolor sit amet, consectetur adipiscing elit");

        assertThat(chunks)
                .zipSatisfy(
                        Arrays.asList("Lorem ipsum dolor ", "sit amet, ", "consectetur ", "adipiscing elit"),
                        (chunk, end) -> assertThat(chunk)
                                .startsWith("Outgoing Response")
                                .endsWith(end));
    }

    @Test
    void shouldFailOnInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new ChunkingSink(delegate, 0));
    }

    @Test
    void shouldCreateWithSizeOfOne() {
        assertDoesNotThrow(() -> new ChunkingSink(delegate, 1));
    }

    private List<String> captureResponse(final String response) throws IOException {
        unit.write(new SimpleCorrelation("id", MIN, MIN), MockHttpRequest.create(),
                MockHttpResponse.create().withBodyAsString(response));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, atLeastOnce()).write(any(), captor.capture());
        return captor.getAllValues();
    }

}
