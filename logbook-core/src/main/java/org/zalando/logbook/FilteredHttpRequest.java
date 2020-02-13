package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;

import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@AllArgsConstructor(access = PRIVATE)
final class FilteredHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;

    private final String query;
    private final String path;
    private final BodyFilter bodyFilter;

    private final HttpHeaders headers;

    FilteredHttpRequest(final HttpRequest request,
            final QueryFilter queryFilter,
            final PathFilter pathFilter, 
            final HeaderFilter headerFilter,
            final BodyFilter bodyFilter) {
        this.request = request;
        this.bodyFilter = bodyFilter;
        this.headers = headerFilter.filter(request.getHeaders());
        
        final String query = request.getQuery();
        this.query = query.isEmpty() ? query : queryFilter.filter(query);
        
        this.path = pathFilter.filter(request.getPath());
    }

    @Override
    public HttpRequest delegate() {
        return request;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public String getRequestUri() {
        return RequestURI.reconstruct(this);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return withRequest(request.withBody());
    }

    @Override
    public HttpRequest withoutBody() {
        return withRequest(request.withoutBody());
    }

    private FilteredHttpRequest withRequest(final HttpRequest request) {
        return new FilteredHttpRequest(request, query, path, bodyFilter, headers);
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
