package org.zalando.logbook.jdkhttpclient;

import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Optional;

final class LocalRequest implements org.zalando.logbook.HttpRequest {

    private final HttpRequest request;
    private final BufferingBodyPublisher bodyPublisher;
    private boolean withBody;

    LocalRequest(final HttpRequest request, final BufferingBodyPublisher bodyPublisher) {
        this.request = request;
        this.bodyPublisher = bodyPublisher;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return request.version().map(Object::toString).orElse("HTTP/1.1");
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.method();
    }

    @Override
    public String getScheme() {
        return request.uri().getScheme();
    }

    @Override
    public String getHost() {
        return request.uri().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.uri().getPort()).filter(port -> port != -1);
    }

    @Override
    public String getPath() {
        return request.uri().getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(request.uri().getQuery()).orElse("");
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(request.headers().map());
    }

    @Override
    public byte[] getBody() {
        return withBody && bodyPublisher != null ? bodyPublisher.getBody() : new byte[0];
    }

    @Override
    public LocalRequest withBody() {
        withBody = true;
        return this;
    }

    @Override
    public LocalRequest withoutBody() {
        withBody = false;
        return this;
    }
}
