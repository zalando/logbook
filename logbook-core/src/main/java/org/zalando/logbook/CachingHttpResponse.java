package org.zalando.logbook;

import java.util.List;
import java.util.Map;

final class CachingHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;
    private final Cache<Map<String, List<String>>> headers;

    CachingHttpResponse(final HttpResponse response) {
        this.response = response;
        this.headers = new Cache<>(response::getHeaders);
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers.get();
    }

}
