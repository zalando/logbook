package org.zalando.logbook;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface HeaderFilter {

    Map<String, List<String>> filter(final Map<String, List<String>> headers);

    static HeaderFilter none() {
        return headers -> headers;
    }

    static HeaderFilter merge(final HeaderFilter left, final HeaderFilter right) {
        return headers ->
                left.filter(right.filter(headers));
    }

}
