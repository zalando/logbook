package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ForwardingHttpExchangeTest {

    private final MockHttpExchange exchange = new MockHttpExchange();

    private final ForwardingHttpExchange unit = new ForwardingHttpExchange(null, exchange, null);

    @Test
    public void shouldReturnRequestHeadersFromForward() {
        Headers headers = unit.getRequestHeaders();
        assertEquals("h2value1", headers.getFirst("header2"));
        assertEquals(Arrays.asList("h1value1", "h1value2"), headers.get("header1"));
    }

    @Test
    public void shouldReturnResponseHeadersFromForward() {
        Headers headers = unit.getResponseHeaders();
        assertEquals("h2value1", headers.getFirst("response-header2"));
        assertEquals(Arrays.asList("h1value1", "h1value2"), headers.get("response-header1"));
    }

    @Test
    public void shouldReturnRequestURIFromForward() {
        assertEquals(exchange.getRequestURI(), unit.getRequestURI());
    }

    @Test
    public void shouldReturnRequestMethodFromForward() {
        assertEquals(exchange.getRequestMethod(), unit.getRequestMethod());
    }

    @Test
    public void shouldReturnHttpContextFromForward() {
        assertEquals(exchange.getHttpContext(), unit.getHttpContext());
    }

    @Test
    public void shouldReturnRequestBodyFromForward() {
        assertEquals(exchange.getRequestBody(), unit.getRequestBody());
    }

    @Test
    public void shouldReturnRemoteAddressFromForward() {
        assertEquals(exchange.getRemoteAddress(), unit.getRemoteAddress());
    }

    @Test
    public void shouldReturnLocalAddressFromForward() {
        assertEquals(exchange.getLocalAddress(), unit.getLocalAddress());
    }

    @Test
    public void shouldReturnProtocolFromForward() {
        assertEquals(exchange.getProtocol(), unit.getProtocol());
    }

    @Test
    public void shouldReturnPrincipalFromForward() {
        assertEquals(exchange.getPrincipal(), unit.getPrincipal());
    }

    @Test
    public void shouldReturnResponseCodeFromForward() {
        assertEquals(exchange.getResponseCode(), unit.getResponseCode());
    }

    @Test
    public void shouldReturnConfiguredStreams() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        OutputStream os = new ByteArrayOutputStream();
        unit.setStreams(is, os);
        assertEquals(is, unit.getRequestBody());
        assertEquals(os, unit.getResponseBody());
    }

    @Test
    public void shouldReturnConfiguredAttributes() {
        unit.setAttribute("test", "ok");
        assertEquals("ok", unit.getAttribute("test"));
    }

    @Test
    public void shouldCloseForward() {
        assertFalse(exchange.isClosed());
        unit.close();
        assertTrue(exchange.isClosed());
    }

}
