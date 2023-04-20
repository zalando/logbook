package org.zalando.logbook.jdkserver;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
