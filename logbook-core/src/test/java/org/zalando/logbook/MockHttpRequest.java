package org.zalando.logbook;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Builder;
import lombok.Singular;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MockHttpRequest implements HttpRequest {

    private final String remote;
    private final String method;
    private final String requestUri;
    private final Map<String, String> parameters;
    private final Map<String, String> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @Builder
    public MockHttpRequest(@Nullable final String remote,
            @Nullable final String method,
            @Nullable final String requestUri,
            @Nullable @Singular final Map<String, String> parameters,
            @Nullable @Singular final Map<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.remote = firstNonNull(remote, "127.0.0.1");
        this.method = firstNonNull(method, "GET");
        this.requestUri = firstNonNull(requestUri, "/test");
        this.parameters = firstNonNullNorEmpty(parameters, ImmutableMap.of("limit", "1"));
        this.headers = firstNonNullNorEmpty(headers, ImmutableMap.of(
                "Accept", "application/json",
                "Content-Type", "text/plain"));
        this.contentType = firstNonNull(contentType, "text/plain");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
        this.body = firstNonNull(body, "Hello, world!");
    }

    static <K, V> Map<K, V> firstNonNullNorEmpty(@Nullable final Map<K, V> first, final Map<K, V> second) {
        return first != null && !first.isEmpty() ? first : checkNotNull(second);
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
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public Multimap<String, String> getParameters() {
        return Multimaps.forMap(parameters);
    }

    @Override
    public Multimap<String, String> getHeaders() {
        return Multimaps.forMap(headers);
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
    public byte[] getBody() {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() {
        return body;
    }

    static HttpRequest create() {
        return builder().build();
    }

}
