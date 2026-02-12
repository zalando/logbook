package org.zalando.logbook.core.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import jakarta.annotation.Nonnull;
import java.util.HashMap;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class AllAttributesExtractor implements AttributeExtractor {
    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request) {
        return request.getAttributes();
    }

    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request, HttpResponse response) {
        HttpAttributes requestAttributes = extract(request);
        HttpAttributes responseAttributes = response.getAttributes();
        HashMap<String, Object> allAttributes = new HashMap<>(requestAttributes);
        allAttributes.putAll(responseAttributes);

        return new HttpAttributes(allAttributes);
    }
}
