package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import jakarta.annotation.Nonnull;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public interface AttributeExtractor {

    @Nonnull
    default HttpAttributes extract(final HttpRequest request) {
        return HttpAttributes.EMPTY;
    }

    @Nonnull
    default HttpAttributes extract(final HttpRequest request, final HttpResponse response) {
        return HttpAttributes.EMPTY;
    }

}
