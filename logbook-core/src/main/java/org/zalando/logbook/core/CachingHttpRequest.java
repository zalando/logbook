package org.zalando.logbook.core;

import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.attributes.HttpAttributes;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

final class CachingHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final Cache<HttpHeaders> headers;
    private final HttpAttributes httpAttributes;

    CachingHttpRequest(final HttpRequest request) {
        this(request, HttpAttributes.EMPTY);
    }

    CachingHttpRequest(final HttpRequest request, final HttpAttributes httpAttributes) {
        this.request = request;
        this.headers = new Cache<>(request::getHeaders);
        this.httpAttributes = httpAttributes;
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers.get();
    }

    @Override
    public HttpAttributes getAttributes() {
        return httpAttributes;
    }

}
