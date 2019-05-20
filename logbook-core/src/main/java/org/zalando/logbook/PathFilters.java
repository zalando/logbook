package org.zalando.logbook;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public class PathFilters {

    private PathFilters() {
    }

    public static PathFilter defaultValue() {
        return PathFilter.none();
    }
    
    @API(status = EXPERIMENTAL)
    public static PathFilter replace(final String expression) {
        return new RegexpPathFilter(expression);
    }

}
