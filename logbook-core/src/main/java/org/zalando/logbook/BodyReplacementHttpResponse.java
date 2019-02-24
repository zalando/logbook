package org.zalando.logbook;

import static java.nio.charset.StandardCharsets.UTF_8;

final class BodyReplacementHttpResponse implements ForwardingHttpResponse, HttpResponse {

    private final HttpResponse response;
    private final String replacement;

    public BodyReplacementHttpResponse(final HttpResponse response, final String replacement) {
        this.response = response;
        this.replacement = replacement;
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public byte[] getBody() {
        return replacement.getBytes(UTF_8);
    }

    @Override
    public String getBodyAsString() {
        return replacement;
    }

    @Override
    public HttpResponse withBody() {
        response.withoutBody();
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        // TODO set body to empty string?
        return this;
    }

}
