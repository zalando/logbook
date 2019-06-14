package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apiguardian.api.API.Status.INTERNAL;

// TODO package private
@API(status = INTERNAL)
public final class FilteredHttpRequest implements ForwardingHttpRequest {

    private final HttpRequest request;
    private final String query;
    private final String path;
    private final BodyFilter bodyFilter;
    private final Map<String, List<String>> headers;

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
    public String getRequestUri() {
        return RequestURI.reconstruct(this);
    }

    @Override
    public String getQuery() {
        return query;
    }
    
    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    @Override
    public String getPath() {
        return path;
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
