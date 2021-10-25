package org.zalando.logbook;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
final class BodyReplacementHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final String replacement;

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Nullable
    @Override
    public Object getNativeRequest() {
        return delegate().getNativeRequest();
    }

    @Override
    public HttpRequest withBody() {
        return withoutBody();
    }

    @Override
    public HttpRequest withoutBody() {
        return new BodyReplacementHttpRequest(request.withoutBody(), replacement);
    }

    @Override
    public byte[] getBody() {
        return replacement.getBytes(UTF_8);
    }

    @Override
    public String getBodyAsString() {
        return replacement;
    }

}
