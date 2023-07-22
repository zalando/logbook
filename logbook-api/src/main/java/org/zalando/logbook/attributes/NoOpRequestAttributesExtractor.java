package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;

import javax.annotation.Nonnull;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public class NoOpRequestAttributesExtractor implements RequestAttributesExtractor {

    @Nonnull
    @Override
    public HttpAttributes extract(HttpRequest request) {
        return HttpAttributes.EMPTY;
    }

}
