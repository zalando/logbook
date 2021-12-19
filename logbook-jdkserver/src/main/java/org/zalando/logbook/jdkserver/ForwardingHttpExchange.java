package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import lombok.RequiredArgsConstructor;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

@RequiredArgsConstructor
final class ForwardingHttpExchange extends HttpExchange {

    private final Response response;

    private final HttpExchange httpExchange;

    private final Logbook.ResponseProcessingStage responseProcessingStage;

    private Logbook.ResponseWritingStage responseWritingStage;

    public Logbook.ResponseWritingStage getResponseWritingStage() {
        return responseWritingStage;
    }

    @Override
    public Headers getRequestHeaders() {
        return httpExchange.getRequestHeaders();
    }

    @Override
    public Headers getResponseHeaders() {
        return httpExchange.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return httpExchange.getHttpContext();
    }

    @Override
    public void close() {
        httpExchange.close();
    }

    @Override
    public InputStream getRequestBody() {
        return httpExchange.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
        responseWritingStage = responseProcessingStage.process(response);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return httpExchange.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return httpExchange.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return httpExchange.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return httpExchange.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return httpExchange.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        httpExchange.setAttribute(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        httpExchange.setStreams(i, o);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpExchange.getPrincipal();
    }
}
