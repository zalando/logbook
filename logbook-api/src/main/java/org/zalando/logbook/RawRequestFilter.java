package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface RawRequestFilter {

    RawHttpRequest filter(final RawHttpRequest request);

    static RawRequestFilter none() {
        return request -> request;
    }

    static RawRequestFilter merge(final RawRequestFilter left, final RawRequestFilter right) {
        return request ->
                left.filter(right.filter(request));
    }

}
