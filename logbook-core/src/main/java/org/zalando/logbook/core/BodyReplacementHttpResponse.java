package org.zalando.logbook.core;

import lombok.AllArgsConstructor;
import org.zalando.logbook.ForwardingHttpResponse;
import org.zalando.logbook.HttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
final class BodyReplacementHttpResponse implements ForwardingHttpResponse, HttpResponse {

    private final HttpResponse response;
    private final String replacement;

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public HttpResponse withBody() {
        return withoutBody();
    }

    @Override
    public HttpResponse withoutBody() {
        return new BodyReplacementHttpResponse(response.withoutBody(), replacement);
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
