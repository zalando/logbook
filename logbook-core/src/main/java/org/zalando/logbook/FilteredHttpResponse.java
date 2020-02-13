package org.zalando.logbook;

import lombok.AllArgsConstructor;
import org.apiguardian.api.API;

import java.io.IOException;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@AllArgsConstructor
final class FilteredHttpResponse implements ForwardingHttpResponse {

    private final HttpResponse response;
    private final HeaderFilter headerFilter;
    private final BodyFilter bodyFilter;

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

}
