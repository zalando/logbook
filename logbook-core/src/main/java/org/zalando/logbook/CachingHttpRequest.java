package org.zalando.logbook;

import java.util.List;
import java.util.Map;

final class CachingHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final Cache<Map<String, List<String>>> headers;

    CachingHttpRequest(final HttpRequest request) {
        this.request = request;
        this.headers = new Cache<>(request::getHeaders);
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers.get();
    }

}
