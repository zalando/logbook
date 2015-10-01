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
import static org.zalando.logbook.MockHttpRequest.firstNonNullNorEmpty;

final class MockHttpResponse implements HttpResponse {

    private final int status;
    private final Map<String, String> headers;
    private final String contentType;
    private final Charset charset;
    private final String body;

    @Builder
    public MockHttpResponse(final int status,
            @Nullable @Singular final Map<String, String> headers,
            @Nullable final String contentType,
            @Nullable final Charset charset,
            @Nullable final String body) {
        this.status = status == 0 ? 200 : status;
        this.headers = firstNonNullNorEmpty(headers, ImmutableMap.of("Content-Type", "application/json"));
        this.contentType = firstNonNull(contentType, "application/json");
        this.charset = firstNonNull(charset, StandardCharsets.UTF_8);
        this.body = firstNonNull(body, "{\"success\":true}");
    }

    @Override
    public int getStatus() {
        return status;
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

    static HttpResponse create() {
        return builder().build();
    }

}
