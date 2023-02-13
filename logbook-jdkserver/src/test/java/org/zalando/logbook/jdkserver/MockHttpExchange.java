package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class MockHttpExchange extends HttpExchange {

    static final String REQUEST_BODY = "request";

    private final URI uri;

    private final ByteArrayInputStream defaultInputStream =
            new ByteArrayInputStream(REQUEST_BODY.getBytes(StandardCharsets.UTF_8));

    private final ByteArrayOutputStream defaultOutputStream =
            new ByteArrayOutputStream();

    private final HttpContext httpContext = Mockito.mock(HttpContext.class);

    private final HttpPrincipal httpPrincipal = Mockito.mock(HttpPrincipal.class);

    private final Map<String,Object> attributes = new HashMap<>();

    private InputStream configuredInputStream;

    private OutputStream configuredOutputStream;

    private boolean closed;

    public MockHttpExchange() {
        this("http://test/path?query=1&other=2");
    }

    public MockHttpExchange(String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Headers getRequestHeaders() {
        Headers headers = new Headers();
        headers.put("header1", Arrays.asList("h1value1", "h1value2"));
        headers.put("header2", Collections.singletonList("h2value1"));
        return headers;
    }

    @Override
    public Headers getResponseHeaders() {
        Headers headers = new Headers();
        headers.put("response-header1", Arrays.asList("h1value1", "h1value2"));
        headers.put("response-header2", Collections.singletonList("h2value1"));
        return headers;
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public String getRequestMethod() {
        return "GET";
    }

    @Override
    public HttpContext getHttpContext() {
        return httpContext;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public InputStream getRequestBody() {
        if (configuredInputStream != null) {
            return configuredInputStream;
        }
        return defaultInputStream;
    }

    @Override
    public OutputStream getResponseBody() {
        if (configuredOutputStream != null) {
            return configuredOutputStream;
        }
        return defaultOutputStream;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {

    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress("remote", 9999);
    }

    @Override
    public int getResponseCode() {
        return 200;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(9999);
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        this.configuredInputStream = i;
        this.configuredOutputStream = o;
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpPrincipal;
    }

    public boolean isClosed() {
        return closed;
    }

}
