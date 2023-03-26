package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static java.time.Clock.fixed;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final public class ExtendedLogFormatSinkTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @Test
    void shouldDelegateActive() {
        final Sink unit = new ExtendedLogFormatSink(writer);
        assertFalse(unit.isActive());
    }

    @Test
    void shouldDelegateInactive() {
        final Sink unit = new ExtendedLogFormatSink(writer);
        when(writer.isActive()).thenReturn(true);
        assertTrue(unit.isActive());
    }

    @Test
    void shouldNotWriteRequestBeforeResponse() throws IOException {
        final Clock clock = fixed(Instant.parse("2019-08-02T08:04:27Z"), UTC);
        final Precorrelation correlation = new DefaultLogbook.SimplePrecorrelation("", clock);

        new ExtendedLogFormatSink(writer)
                .write(correlation, MockHttpRequest.create());

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void testDefaultExpression() throws IOException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", Collections.singletonList("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0"));
        headers.put("Cookie", Collections.singletonList("name=value"));
        headers.put("Referrer", Collections.singletonList("https://example.com/page?q=123"));
        final String output = test(
                MockHttpRequest.create()
                        .withRemote("185.85.220.253")
                        .withMethod("POST")
                        .withProtocolVersion("HTTP/1.1")
                        .withPath("/search")
                        .withQuery("q=zalando")
                        .withBodyAsString("{\"request\": \"value\"}")
                        .withHeaders(HttpHeaders.of(headers)),
                MockHttpResponse.create()
                        .withStatus(200)
                        .withBodyAsString("{\"response\": \"value\"}"));

        assertEquals("2019-08-02 08:16:41 185.85.220.253 localhost POST /search ?q=zalando 200 21 20 0.125 HTTP/1.1 \"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0\" \"name=value\" \"https://example.com/page?q=123\"", output);
    }

    @Test
    void testMultipleRequestHeaders() throws IOException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cookie", Arrays.asList("name1=value1", "name2=value2", "name3=value3"));
        final String output = test(
                MockHttpRequest.create()
                        .withRemote("185.85.220.253")
                        .withMethod("POST")
                        .withPath("/search")
                        .withQuery("q=zalando")
                        .withHeaders(HttpHeaders.of(headers)),
                MockHttpResponse.create());

        assertEquals("2019-08-02 08:16:41 185.85.220.253 localhost POST /search ?q=zalando 200 0 0 0.125 HTTP/1.1 - \"name1=value1;name2=value2;name3=value3\" -", output);
    }

    @Test
    void testCustomRequestHeaders() throws IOException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Header-One", Collections.singletonList("val1"));
        headers.put("Header-Two", Collections.singletonList("val2"));
        final String output = test(
                MockHttpRequest.create().withHeaders(HttpHeaders.of(headers)),
                MockHttpResponse.create(),
                "cs(Header-One) cs(Header-Two)");

        assertEquals("\"val1\" \"val2\"", output);
    }

    @Test
    void testResponseHeaders() throws IOException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Header-One", Collections.singletonList("val1"));
        headers.put("Header-Two", Collections.singletonList("val2"));
        final String output = test(
                MockHttpRequest.create(),
                MockHttpResponse.create().withHeaders(HttpHeaders.of(headers)),
                "sc(Header-One) sc(Header-Two)");

        assertEquals("\"val1\" \"val2\"", output);
    }

    @Test
    void testInvalidFields() throws IOException {
        final String output = test(
                MockHttpRequest.create()
                        .withRemote("185.85.220.253")
                        .withMethod("POST")
                        .withPath("/search")
                        .withQuery("q=zalando"),
                MockHttpResponse.create(),
                "date time sc() sc( cs() abc() )");

        assertEquals("2019-08-02 08:16:41 - - - - -", output);
    }

    @Test
    void testEmptyFields() throws IOException {
        final String output = test(
                MockHttpRequest.create()
                        .withRemote("185.85.220.253")
                        .withMethod("POST")
                        .withPath("/search")
                        .withQuery("q=zalando"),
                MockHttpResponse.create(),
                "");
        assertEquals("2019-08-02 08:16:41 185.85.220.253 localhost POST /search ?q=zalando 200 0 0 0.125 HTTP/1.1 - - -", output);
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response) throws IOException {

        final Instant start = Instant.parse("2019-08-02T08:16:41.000Z");
        return test(request, response, start, new ExtendedLogFormatSink(writer));
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response,
            final String fields) throws IOException {

        final Instant start = Instant.parse("2019-08-02T08:16:41.000Z");
        return test(request, response, start, new ExtendedLogFormatSink(writer, fields));
    }

    private String test(
            final HttpRequest request,
            final HttpResponse response,
            final Instant start,
            final Sink sink) throws IOException {

        final Correlation correlation = new DefaultLogbook.SimpleCorrelation(
                "", start, start.plusMillis(125));

        sink.write(correlation, request, response);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(eq(correlation), captor.capture());
        return captor.getValue();
    }

}
