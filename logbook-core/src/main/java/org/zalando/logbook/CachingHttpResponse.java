package org.zalando.logbook;

final class CachingHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;
    private final Cache<HttpHeaders> headers;

    CachingHttpResponse(final HttpResponse response) {
        this.response = response;
        this.headers = new Cache<>(response::getHeaders);
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers.get();
    }

}
