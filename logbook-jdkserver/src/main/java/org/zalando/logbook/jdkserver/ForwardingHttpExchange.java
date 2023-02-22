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

final class ForwardingHttpExchange extends HttpExchange {

    private final HttpExchange httpExchange;

    private final ResponseStream responseStream;

    private OutputStream configuredOutputStream;

    public ForwardingHttpExchange(Response response, HttpExchange httpExchange, Logbook.ResponseProcessingStage responseProcessingStage) {
        this.httpExchange = httpExchange;
        this.responseStream = new ResponseStream(response, responseProcessingStage);
    }

    public Logbook.ResponseWritingStage getResponseWritingStage() {
        return responseStream.getResponseWritingStage();
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
        if (configuredOutputStream != null) {
            return configuredOutputStream;
        }
        return responseStream;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        httpExchange.sendResponseHeaders(rCode, responseLength);
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
        httpExchange.setStreams(i, null);
        configuredOutputStream = o;
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return httpExchange.getPrincipal();
    }

    @RequiredArgsConstructor
    private static class ResponseStream extends OutputStream {

        private final Response response;

        private final Logbook.ResponseProcessingStage responseProcessingStage;

        private Logbook.ResponseWritingStage responseWritingStage;

        private OutputStream os;

        @Override
        public void write(int b) throws IOException {
            getOutputStream().write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            getOutputStream().write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            getOutputStream().write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            getOutputStream().flush();
        }

        @Override
        public void close() throws IOException {
            getOutputStream().close();
        }

        public Logbook.ResponseWritingStage getResponseWritingStage() {
            return responseWritingStage;
        }

        private OutputStream getOutputStream() throws IOException {
            if (os == null) {
                responseWritingStage = responseProcessingStage.process(response);
                os = response.getOutputStream();
            }
            return os;
        }

    }
}
