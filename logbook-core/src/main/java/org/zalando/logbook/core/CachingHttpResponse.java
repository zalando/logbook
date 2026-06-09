package org.zalando.logbook.core;

import org.zalando.logbook.ForwardingHttpResponse;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.HttpAttributes;

import java.io.IOException;

final class CachingHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;
    private final Cache<HttpHeaders> headers;
    private final HttpAttributes httpAttributes;

    CachingHttpResponse(final HttpResponse response) {
        this(response, HttpAttributes.EMPTY);
    }

    CachingHttpResponse(final HttpResponse response, final HttpAttributes httpAttributes) {
        this.response = response;
        this.headers = new Cache<>(response::getHeaders);
        this.httpAttributes = httpAttributes;
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers.get();
    }

    @Override
    public HttpAttributes getAttributes() {
        return httpAttributes;
    }

    @Override
    public HttpResponse withBody() throws IOException {
        // preserve attributes when body is re-attached
        return new CachingHttpResponse(response.withBody(), httpAttributes);
    }

    @Override
    public HttpResponse withoutBody() {
        // preserve attributes when body is stripped — fixes attribute loss
        // in strategies like BodyOnlyIfStatusAtLeastStrategy that call
        // response.withoutBody() on the cached response
        return new CachingHttpResponse(response.withoutBody(), httpAttributes);
    }
}
