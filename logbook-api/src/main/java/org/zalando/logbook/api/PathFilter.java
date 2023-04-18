package org.zalando.logbook.api;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface PathFilter {

    String filter(final String path);

    static PathFilter none() {
        return path -> path;
    }

    static PathFilter merge(final PathFilter left, final PathFilter right) {
        return path ->
                left.filter(right.filter(path));
    }
}
