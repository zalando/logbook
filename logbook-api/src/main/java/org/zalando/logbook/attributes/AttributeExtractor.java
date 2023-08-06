package org.zalando.logbook.attributes;

import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import javax.annotation.Nonnull;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public interface AttributeExtractor {

    default void logException(final Exception exception) {
        // do nothing by default
    }

    @SuppressWarnings("RedundantThrows")
    @Nonnull
    default HttpAttributes extract(final HttpRequest request) throws Exception {
        return HttpAttributes.EMPTY;
    }

    @SuppressWarnings({"unused", "RedundantThrows"})
    @Nonnull
    default HttpAttributes extract(final HttpRequest request, final HttpResponse response) throws Exception {
        return HttpAttributes.EMPTY;
    }

}
