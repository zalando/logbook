package org.zalando.logbook;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface PathFilter {

    String filter(final String path);

    static PathFilter none() {
        return path -> path;
    }

    static PathFilter merge(final PathFilter left, final PathFilter right) {
        return headers ->
                left.filter(right.filter(headers));
    }
}
