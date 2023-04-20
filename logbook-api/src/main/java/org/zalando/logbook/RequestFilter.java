package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface RequestFilter {

    HttpRequest filter(final HttpRequest request);

    static RequestFilter none() {
        return request -> request;
    }

    static RequestFilter merge(final RequestFilter left, final RequestFilter right) {
        return request ->
                left.filter(right.filter(request));
    }

}
