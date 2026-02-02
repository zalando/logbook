package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class RequestTest {

    private final Request unit = new Request(new MockHttpExchange());

    @Test
    public void shouldReturnOriginFromExchange() {
        assertEquals(Origin.REMOTE, unit.getOrigin());
    }

    @Test
    public void shouldReturnHeadersFromExchange() {
        HttpHeaders headers = unit.getHeaders();
        assertEquals("h2value1", headers.getFirst("header2"));
        assertEquals(Arrays.asList("h1value1", "h1value2"), headers.get("header1"));
    }

    @Test
    public void shouldReturnRemoteFromExchange() {
        assertEquals("remote", unit.getRemote());
    }

    @Test
    public void shouldReturnMethodFromExchange() {
        assertEquals("GET", unit.getMethod());
    }

    @Test
    public void shouldReturnSchemeFromExchange() {
        assertEquals("http", unit.getScheme());
    }

    @Test
    public void shouldReturnHostFromExchange() {
        assertEquals("0.0.0.0", unit.getHost());
    }

    @Test
    public void shouldReturnPortFromExchange() {
        assertEquals(9999, unit.getPort().get());
    }

    @Test
    public void shouldReturnPathFromExchange() {
        assertEquals("/path", unit.getPath());
    }

    @Test
    public void shouldReturnQueryFromExchange() {
        assertEquals("query=1&other=2", unit.getQuery());
    }

    @Test
    public void shouldReturnProtocolVersionFromExchange() {
        assertEquals("HTTP/1.1", unit.getProtocolVersion());
    }

    @Test
    public void shouldReturnRequestUriFromExchange() {
        assertEquals("http://test/path?query=1&other=2", unit.getRequestUri());
    }

    @Test
    public void shouldReturnNotNullPath() {
        assertEquals("",
                new Request(new MockHttpExchange("http://test")).getPath());
    }

    @Test
    public void shouldReturnNotNullQuery() {
        assertEquals("",
                new Request(new MockHttpExchange("http://test")).getQuery());
    }

    @Test
    public void shouldReturnNotNullScheme() {
        assertEquals("",
                new Request(new MockHttpExchange("test")).getScheme());
    }

    @Test
    public void withBody_eagerlyBuffersBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test body content".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();

        assertEquals(17, request.getBody().length);
        final InputStream closedStream = mock(InputStream.class);
        when(closedStream.read((byte[]) any())).thenThrow(new IOException("Stream closed"));
        when(exchange.getRequestBody()).thenReturn(closedStream);

        assertEquals(17, request.getBody().length);
    }

    @Test
    public void withoutBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestBody()).thenReturn(mock(InputStream.class));

        Request request = new Request(exchange);
        final var result = request.withoutBody();

        assertEquals(request, result);
        assertEquals(0, request.getBody().length);
    }

    @Test
    public void withBody_thenWithoutBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test body content".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();
        assertEquals(17, request.getBody().length);

        final var result = request.withoutBody();
        assertEquals(request, result);
        assertEquals(0, request.getBody().length);
    }

    @Test
    public void offered_withoutBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();
        byte[] body = request.getBody();
        assertEquals(4, body.length);

        request.withoutBody();
        assertEquals(0, request.getBody().length);

        request.withBody();
        assertEquals(4, request.getBody().length);
    }

    @Test
    public void unbuffered_buffer_viaGetBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "direct".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        byte[] body = request.getBody();
        assertEquals(6, body.length);
    }

    @Test
    public void offered_withBody_twice() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();
        byte[] body = request.getBody();
        assertEquals(4, body.length);

        request.withBody();
        assertEquals(4, request.getBody().length);
    }

    @Test
    public void offered_without_thenGetInputStream() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();
        assertEquals(4, request.getBody().length);

        InputStream stream = request.getInputStream();
        assertEquals(4, request.getBody().length);
    }

    @Test
    public void ignoring_getInputStream_returnEmpty() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test body".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();
        assertEquals(9, request.getBody().length);

        request.withoutBody();

        InputStream stream = request.getInputStream();
        byte[] streamData = new byte[10];
        int bytesRead = stream.read(streamData);
        assertEquals(-1, bytesRead);
    }

    @Test
    public void withoutBody_thenWithBody_stateTransitions() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "new body".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        // Start in Unbuffered, transition to Withouted via withoutBody
        request.withoutBody();

        // Then transition back to Offered via withBody
        request.withBody();

        // getBody should now trigger buffering from Offered
        assertEquals(8, request.getBody().length);
    }

    @Test
    public void offered_without_returnsWithouted() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();  // Unbuffered -> Offered
        assertEquals(4, request.getBody().length);

        request.withoutBody();  // Offered -> Withouted (via Buffering -> Ignoring)
        assertEquals(0, request.getBody().length);
    }

    @Test
    public void unbuffered_withoutBody_thenWithBody() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "test".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        // Start Unbuffered, transition to Withouted via withoutBody
        request.withoutBody();
        assertEquals(0, request.getBody().length);

        // Then back to Offered via withBody, then buffer
        request.withBody();
        assertEquals(4, request.getBody().length);
    }

    @Test
    public void offered_without_beforeBuffer() throws Exception {
        final HttpExchange exchange = mock(HttpExchange.class);
        final byte[] bodyContent = "data".getBytes();
        final InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(bodyContent, 0, buffer, 0, bodyContent.length);
            return bodyContent.length;
        }).thenReturn(-1);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        Request request = new Request(exchange);
        request.withBody();  // Unbuffered -> Offered

        // Call withoutBody BEFORE getBody, so it's called on Offered state itself
        request.withoutBody();  // Offered.without() -> Withouted

        // Now getBody should return empty since we're in Withouted state
        assertEquals(0, request.getBody().length);
    }

}
