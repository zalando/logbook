package org.zalando.logbook;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BodyReplacementHttpResponse implements ForwardingHttpResponse, HttpResponse {

    private final HttpResponse response;
    private final String replacement;

    public BodyReplacementHttpResponse(final HttpResponse response, final String replacement) {
        this.response = response.withoutBody();
        this.replacement = replacement;
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public HttpResponse withBody() {
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
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
