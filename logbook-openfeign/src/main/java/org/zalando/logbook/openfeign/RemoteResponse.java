package org.zalando.logbook.openfeign;

import feign.Response;
import lombok.RequiredArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Optional;

@RequiredArgsConstructor
final class RemoteResponse implements HttpResponse {
    private final Response response;
    private final HttpHeaders headers;
    private final byte[] body;
    private boolean withBody = false;

    public static RemoteResponse create(Response response, byte[] body) {
        return new RemoteResponse(
                response,
                HeaderUtils.toLogbookHeaders(response.headers()),
                body
        );
    }

    @Override
    public int getStatus() {
        return response.status();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
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
        return response.charset();
    }

    @Override
    public Object getNativeResponse() {
        return response;
    }

    @Override
    public HttpResponse withBody() {
        withBody = true;
        return this;
    }

    @Override
    public RemoteResponse withoutBody() {
        withBody = false;
        return this;
    }

    @Override
    public byte[] getBody() {
        return withBody && body != null ? body : new byte[0];
    }
}
