package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import static java.time.Instant.MIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
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
    void shouldDelegateActive() {
        assertThat(unit.isActive(), is(false));
    }

    @Test
    void shouldWriteSingleRequestIfLengthNotExceeded() throws IOException {
        final List<String> chunks = captureRequest("HelloWorld");
        assertThat(chunks, contains(
                allOf(startsWith("Incoming Request"), endsWith("HelloWorld"))));
    }

    @Test
    void shouldWriteRequestInChunksIfLengthExceeded() throws IOException {
        final List<String> chunks = captureRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        assertThat(chunks, contains(
                allOf(startsWith("Incoming Request"), endsWith("Lorem ipsum dolor ")),
                allOf(startsWith("Incoming Request"), endsWith("sit amet, ")),
                allOf(startsWith("Incoming Request"), endsWith("consectetur ")),
                allOf(startsWith("Incoming Request"), endsWith("adipiscing elit"))));
    }

    private List<String> captureRequest(final String request) throws IOException {
        unit.write(new SimplePrecorrelation(Clock.systemUTC()), MockHttpRequest.create().withBodyAsString(request));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, atLeastOnce()).write(any(Precorrelation.class), captor.capture());
        return captor.getAllValues();
    }

    @Test
    void shouldWriteSingleResponseIfLengthNotExceeded() throws IOException {
        final List<String> chunks = captureResponse("HelloWorld");
        assertThat(chunks, contains(
                allOf(startsWith("Outgoing Response"), endsWith("HelloWorld"))));

    }

    @Test
    void shouldWriteResponseInChunksIfLengthExceeded() throws IOException {
        final List<String> chunks = captureResponse("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        assertThat(chunks, contains(
                allOf(startsWith("Outgoing Response"), endsWith("Lorem ipsum dolor ")),
                allOf(startsWith("Outgoing Response"), endsWith("sit amet, ")),
                allOf(startsWith("Outgoing Response"), endsWith("consectetur ")),
                allOf(startsWith("Outgoing Response"), endsWith("adipiscing elit"))));
    }

    @Test
    void shouldFailOnInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new ChunkingSink(delegate, 0));
    }

    @Test
    void shouldCreateWithSizeOfOne() {
        new ChunkingSink(delegate, 1);
    }

    private List<String> captureResponse(final String response) throws IOException {
        unit.write(new SimpleCorrelation("id", MIN, MIN), MockHttpRequest.create(),
                MockHttpResponse.create().withBodyAsString(response));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, atLeastOnce()).write(any(), captor.capture());
        return captor.getAllValues();
    }
}
