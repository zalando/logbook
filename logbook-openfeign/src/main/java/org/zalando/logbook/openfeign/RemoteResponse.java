package org.zalando.logbook.openfeign;

import feign.Response;
import lombok.RequiredArgsConstructor;
import org.zalando.logbook.api.HttpHeaders;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Optional;

@RequiredArgsConstructor
final class RemoteResponse implements HttpResponse {
    private final int status;
    private final HttpHeaders headers;
    private final byte[] body;
    private final Charset charset;
    private boolean withBody = false;

    public static RemoteResponse create(Response response, byte[] body) {
        return new RemoteResponse(
                response.status(),
                HeaderUtils.toLogbookHeaders(response.headers()),
                body,
                response.charset()
        );
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getProtocolVersion() {
        // feign doesn't support HTTP/2, their own toString looks like this:
        // builder.append(httpMethod).append(' ').append(url).append(" HTTP/1.1\n");
        return "HTTP/1.1";
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
        return charset;
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
