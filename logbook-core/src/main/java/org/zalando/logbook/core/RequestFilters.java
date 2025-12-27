package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyReplacer;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RequestFilter;

import jakarta.annotation.Nullable;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.core.DefaultFilters.defaultValues;

@API(status = STABLE)
public final class RequestFilters {

    private RequestFilters() {

    }

    @API(status = MAINTAINED)
    public static RequestFilter defaultValue() {
        return defaultValues(RequestFilter.class).stream()
                .reduce(replaceBody(BodyReplacers.defaultValue()), RequestFilter::merge);
    }

    public static RequestFilter replaceBody(final BodyReplacer<HttpRequest> replacer) {
        return request -> {
            @Nullable final String replacement = replacer.replace(request);
            if (replacement == null) {
                return request;
            }
            return new BodyReplacementHttpRequest(request, replacement);
        };
    }

}
