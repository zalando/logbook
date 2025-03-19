package org.zalando.logbook.core;

import java.io.IOException;
import lombok.AllArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ForwardingHttpRequest;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.PathFilter;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.RequestURI;
import org.zalando.logbook.attributes.HttpAttributes;

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
    private final HttpAttributes attributes;

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
        this.attributes = request.getAttributes();
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
    public HttpAttributes getAttributes() {
        return attributes;
    }

    @Override
    public HttpRequest withBody() throws IOException {
        return withRequest(request.withBody(), request.getAttributes());
    }

    @Override
    public HttpRequest withoutBody() {
        return withRequest(request.withoutBody(), request.getAttributes());
    }

    private FilteredHttpRequest withRequest(final HttpRequest request, final HttpAttributes attributes) {
        return new FilteredHttpRequest(request, query, path, bodyFilter, headers, attributes);
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
