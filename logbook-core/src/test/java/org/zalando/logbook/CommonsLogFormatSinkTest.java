package org.zalando.logbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Locale;

import static java.time.Clock.fixed;
import static java.time.ZoneOffset.UTC;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class CommonsLogFormatSinkTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @BeforeEach
    void setUp() {
        Locale.setDefault(US);
    }

    @Test
    void shouldDelegateActive() {
        final Sink unit = new CommonsLogFormatSink(writer);
        assertFalse(unit.isActive());
    }

    @Test
    void shouldDelegateInactive() {
        final Sink unit = new CommonsLogFormatSink(writer);
        when(writer.isActive()).thenReturn(true);
        assertTrue(unit.isActive());
    }

    @Test
    void shouldNotWriteRequestBeforeResponse() throws IOException {
        final Clock clock = fixed(Instant.parse("2019-08-02T08:04:27Z"), UTC);
        final Precorrelation correlation = new SimplePrecorrelation(() -> "", clock);

        new CommonsLogFormatSink(writer)
                .write(correlation, MockHttpRequest.create());

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void testSimpleResponse() throws IOException {
        final String output = test(
                MockHttpRequest.create(),
                MockHttpResponse.create());

        assertEquals("127.0.0.1 - - [02/Aug/2019:08:16:41 0000] \"GET / HTTP/1.1\" 200 -", output);
    }

    @Test
    void testRequestParts() throws IOException {
        final String output = test(
                MockHttpRequest.create()
                        .withRemote("185.85.220.253")
                        .withMethod("POST")
                        .withPath("/search")
                        .withQuery("q=zalando")
                        .withProtocolVersion("HTTP/1.0"),
                MockHttpResponse.create());

        assertEquals("185.85.220.253 - - [02/Aug/2019:08:16:41 0000] \"POST /search?q=zalando HTTP/1.0\" 200 -", output);
    }

    @Test
    void testResponseParts() throws IOException {
        final String output = test(
                MockHttpRequest.create(),
                MockHttpResponse.create()
                        .withStatus(201)
                        .withBodyAsString("Hello world!"));

        assertEquals("127.0.0.1 - - [02/Aug/2019:08:16:41 0000] \"GET / HTTP/1.1\" 201 12", output);
    }

    @Test
    void testResponsePositiveOffset() throws IOException {
        final String output = test(
                MockHttpRequest.create(),
                MockHttpResponse.create(),
                Instant.parse("2019-08-02T07:16:41Z"), ZoneOffset.ofHours(1));

        assertEquals("127.0.0.1 - - [02/Aug/2019:08:16:41 +0100] \"GET / HTTP/1.1\" 200 -", output);
    }

    @Test
    void testResponseNegativeOffset() throws IOException {
        final String output = test(
                MockHttpRequest.create(),
                MockHttpResponse.create(),
                Instant.parse("2019-08-02T09:16:41Z"), ZoneOffset.ofHours(-1));

        assertEquals("127.0.0.1 - - [02/Aug/2019:08:16:41 -0100] \"GET / HTTP/1.1\" 200 -", output);
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response) throws IOException {

        final Instant start = Instant.parse("2019-08-02T08:16:41.000Z");
        return test(request, response, start, new CommonsLogFormatSink(writer, UTC));
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response,
            final Instant start,
            final ZoneOffset zone) throws IOException {
        return test(request, response, start, new CommonsLogFormatSink(writer, zone));
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response,
            final Instant start,
            final Sink sink) throws IOException {

        final Correlation correlation = new SimpleCorrelation(
                "", start, start.plusMillis(125));

        sink.write(correlation, request, response);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(eq(correlation), captor.capture());
        return captor.getValue();
    }

}
