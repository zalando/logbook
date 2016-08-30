package org.zalando.logbook;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

@Immutable
public final class MockRawHttpRequest implements MockHttpMessage, RawHttpRequest {

    private final String protocolVersion;
    private final Origin origin;
    private final String remote;
    private final String method;
    private final String scheme;
    private final String host;
    private final Integer port;
    private final String path;
    private final String query;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @lombok.Builder(builderMethodName = "request", builderClassName = "Builder")
    public MockRawHttpRequest(
            @Nullable final String protocolVersion,
            @Nullable final Origin origin,
            @Nullable final String remote,
            @Nullable final String method,
            @Nullable final String scheme,
            @Nullable final String host,
            @Nullable final Integer port,
            @Nullable final String path,
            @Nullable final String query,
            @Nullable final Map<String, List<String>> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.protocolVersion = Optional.ofNullable(protocolVersion).orElse("HTTP/1.1");
        this.origin = Optional.ofNullable(origin).orElse(Origin.REMOTE);
        this.remote = Optional.ofNullable(remote).orElse("127.0.0.1");
        this.method = Optional.ofNullable(method).orElse("GET");
        this.scheme = Optional.ofNullable(scheme).orElse("http");
        this.host = Optional.ofNullable(host).orElse("localhost");
        this.port = Optional.ofNullable(port).orElse(80);
        this.path = Optional.ofNullable(path).orElse("/");
        this.query = Optional.ofNullable(query).orElse("");
        this.headers = firstNonNullNorEmpty(headers, emptyMap());
        this.contentType = Optional.ofNullable(contentType).orElse("");
        this.charset = Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8);
        this.body = Optional.ofNullable(body).orElse("");
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String getRemote() {
        return remote;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(port);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return MockHttpRequest.request()
                .protocolVersion(protocolVersion)
                .origin(origin)
                .remote(remote)
                .method(method)
                .scheme(scheme)
                .host(host)
                .port(port)
                .path(path)
                .query(query)
                .headers(headers)
                .contentType(contentType)
                .charset(charset)
                .body(body)
                .build();
    }

    public static RawHttpRequest create() {
        return request().build();
    }

}
