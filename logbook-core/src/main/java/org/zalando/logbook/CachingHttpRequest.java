package org.zalando.logbook;

final class CachingHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final Cache<HttpHeaders> headers;

    CachingHttpRequest(final HttpRequest request) {
        this.request = request;
        this.headers = new Cache<>(request::getHeaders);
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers.get();
    }

}
