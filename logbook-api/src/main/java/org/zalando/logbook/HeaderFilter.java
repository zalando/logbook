package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@FunctionalInterface
public interface HeaderFilter {

    HttpHeaders filter(final HttpHeaders headers);

    static HeaderFilter none() {
        return headers -> headers;
    }

    static HeaderFilter merge(final HeaderFilter left, final HeaderFilter right) {
        return headers ->
                left.filter(right.filter(headers));
    }

}
