package org.zalando.logbook;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BodyReplacementHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final String replacement;

    public BodyReplacementHttpRequest(final HttpRequest request, final String replacement) {
        this.request = request;
        this.replacement = replacement;
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public HttpRequest withBody() {
        request.withoutBody();
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
        // TODO set replacement to empty string?!
        return this;
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
