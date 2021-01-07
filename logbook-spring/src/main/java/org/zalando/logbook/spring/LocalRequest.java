package org.zalando.logbook.spring;

import org.springframework.http.MediaType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

final class LocalRequest implements HttpRequest {

    private final org.springframework.http.HttpRequest request;
    private final byte[] body;

    private boolean withBody = false;

    LocalRequest(org.springframework.http.HttpRequest request, byte[] body) {
        this.request = request;
        this.body = body;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getScheme() {
        return Optional.of(request.getURI())
                .map(URI::getScheme)
                .orElse("");
    }

    @Override
    public String getHost() {
        return Optional.of(request.getURI())
                .map(URI::getHost)
                .orElse("");
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.getURI().getPort())
                .filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return Optional.of(request.getURI())
                .map(URI::getPath)
                .orElse("");
    }

    @Override
    public String getQuery() {
        return Optional.of(request.getURI())
                .map(URI::getQuery)
                .orElse("");
    }

    @Override
    public HttpRequest withBody() throws IOException {
        withBody = true;
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        withBody = false;
        return this;
    }

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(request.getHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional
                .ofNullable(request.getHeaders().getFirst("Content-Type"))
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(getContentType())
                .map(ct -> MediaType.parseMediaType(ct).getCharset())
                .orElse(UTF_8);
    }

    @Override
    public byte[] getBody() throws IOException {
        return withBody ? body : new byte[0];
    }
}
