package org.zalando.logbook.openfeign;

import feign.Request;
import lombok.RequiredArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
final class LocalRequest implements HttpRequest {
    private final Request request;
    private final URI uri;
    private final HttpHeaders headers;
    private final byte[] body;
    private boolean withBody = false;

    public static LocalRequest create(Request request) {
        return new LocalRequest(
                request,
                URI.create(request.url()),
                HeaderUtils.toLogbookHeaders(request.headers()),
                request.body()
        );
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.httpMethod().toString();
    }

    @Override
    public String getScheme() {
        return uri.getScheme() == null ? "" : uri.getScheme();
    }

    @Override
    public String getHost() {
        return uri.getHost() == null ? "" : uri.getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(uri).map(URI::getPort).filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return uri.getPath() == null ? "" : uri.getPath();
    }

    @Override
    public String getQuery() {
        return uri.getQuery() == null ? "" : uri.getQuery();
    }

    @Override
    public HttpRequest withBody() {
        withBody = true;
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        withBody = false;
        return this;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(headers.get("Content-Type"))
                .flatMap(ct -> ct.stream().findFirst())
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(request.charset())
                .orElse(UTF_8);
    }

    @Override
    public Object getNativeRequest() {
        return request;
    }

    @Override
    public byte[] getBody() {
        return withBody && body != null ? body : new byte[0];
    }
}
