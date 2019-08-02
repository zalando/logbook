package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public class PathFilters {

    private PathFilters() {
    }

    public static PathFilter defaultValue() {
        return PathFilter.none();
    }
    
    @API(status = EXPERIMENTAL)
    public static PathFilter replace(final String expression, final String replacement) {
        return new DefaultPathFilter(replacement, expression);
    }

}
