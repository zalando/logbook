package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import javax.annotation.Nonnull;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public class AllAttributesExtractor implements AttributeExtractor {
    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request) {
        return extractAll(request);
    }

    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request, HttpResponse response) {
        return extractAll(request);
    }

    private HttpAttributes extractAll(HttpRequest request) {
        return request.getAttributes();
    }
}
