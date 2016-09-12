package org.zalando.logbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class FilteredHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final QueryFilter queryFilter;
    private final BodyFilter bodyFilter;
    private final Map<String, List<String>> headers;

    FilteredHttpRequest(final HttpRequest request,
            final QueryFilter queryFilter,
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter) {
        this.request = request;
        this.queryFilter = queryFilter;
        this.bodyFilter = bodyFilter;
        this.headers = headerFilter.filter(request.getHeaders());
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public String getRequestUri() {
        return RequestURI.reconstruct(this);
    }

    @Override
    public String getQuery() {
        final String query = request.getQuery();
        return query.isEmpty() ? query : queryFilter.filter(query);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        return bodyFilter.filter(getContentType(), request.getBodyAsString());
    }

}
