package org.zalando.logbook.core;

import java.io.IOException;
import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.ForwardingHttpResponse;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.HttpAttributes;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
final class FilteredHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;
    private final HeaderFilter headerFilter;
    private final BodyFilter bodyFilter;
    private final HttpAttributes attributes;

    FilteredHttpResponse(final HttpResponse response,
                         final HeaderFilter headerFilter,
                         final BodyFilter bodyFilter) {
        this.response = response;
        this.headerFilter = headerFilter;
        this.bodyFilter = bodyFilter;
        this.attributes = response.getAttributes();
    }

    @Override
    public HttpResponse delegate() {
        return response;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headerFilter.filter(response.getHeaders());
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
        return new FilteredHttpResponse(response, headerFilter, bodyFilter);
    }

    @Override
    public byte[] getBody() throws IOException {
        return getBodyAsString().getBytes(getCharset());
    }

    @Override
    public String getBodyAsString() throws IOException {
        // TODO do this only once?!
        return bodyFilter.filter(response.getContentType(), response.getBodyAsString());
    }

    @Override
    public HttpAttributes getAttributes() {
        return attributes;
    }
}
