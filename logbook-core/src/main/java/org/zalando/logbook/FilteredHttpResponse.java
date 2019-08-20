package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@AllArgsConstructor(access = PRIVATE)
final class FilteredHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;

    private final BodyFilter bodyFilter;
    private final Map<String, List<String>> headers;

    FilteredHttpResponse(final HttpResponse response, final HeaderFilter headerFilter,
            final BodyFilter bodyFilter) {
        this.response = response;
        this.bodyFilter = bodyFilter;
        this.headers = headerFilter.filter(response.getHeaders());
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public HttpResponse withBody() throws IOException {
        return withResponse(response.withBody());
    }

    @Override
    public HttpResponse withoutBody() {
        return withResponse(response.withoutBody());
    }

    private HttpResponse withResponse(final HttpResponse response) {
        return new FilteredHttpResponse(response, bodyFilter, headers);
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return bodyFilter.filter(response.getContentType(), response.getBodyAsString());
    }

}
