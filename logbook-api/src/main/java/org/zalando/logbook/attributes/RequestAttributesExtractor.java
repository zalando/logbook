package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public abstract class RequestAttributesExtractor {

    public final HttpAttributes extractOrEmpty(final HttpRequest request) {
        try {
            return extract(request);
        } catch (Exception e) {
            return HttpAttributes.EMPTY;
        }
    }

    @Nonnull
    protected abstract HttpAttributes extract(final HttpRequest request) throws Exception;

}
