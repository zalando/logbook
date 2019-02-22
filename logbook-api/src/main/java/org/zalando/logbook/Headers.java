package org.zalando.logbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class Headers {

    private Headers() {

    }

    public static Map<String, List<String>> empty() {
        return new TreeMap<>(CASE_INSENSITIVE_ORDER);
    }

    public static Map<String, List<String>> immutableCopy(final Map<String, List<String>> headers) {
        final Map<String, List<String>> copy = empty();

        headers.forEach((header, values) ->
                copy.put(header, unmodifiableList(new ArrayList<>(values))));

        return unmodifiableMap(copy);
    }

}
