package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.With;
import lombok.experimental.Delegate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("deprecation") // needed because of @Delegate and @Deprecated
@AllArgsConstructor(access = PRIVATE)
final class DefaultHttpHeaders
        // gives us a meaningful equals, hashCode and toString
        extends AbstractMap<String, List<String>>
        implements UpdateHttpHeaders, ApplyHttpHeaders, DeleteHttpHeaders {

    static final HttpHeaders EMPTY = new DefaultHttpHeaders();

    @With
    private final TreeMap<String, List<String>> headers;

    private DefaultHttpHeaders() {
        this(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    }

    @Delegate
    @SuppressWarnings("unused")
    private Map<String, List<String>> delegate() {
        return headers;
    }

    @Override
    public HttpHeaders update(
            final String name,
            final Collection<String> values) {

        TreeMap<String, List<String>> updatedHeaders = associate(headers, name, values);
        return withHeaders(updatedHeaders);
    }

    @Override
    public HttpHeaders delete(final Collection<String> names) {
        return withHeaders(delete(headers, names));
    }

    public static List<String> immutableCopy(final Collection<String> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static TreeMap<String, List<String>> associate(
            final TreeMap<String, List<String>> original,
            final String key,
            final Collection<String> value) {

        if (Objects.equals(original.get(key), value))
            return original;

        TreeMap<String, List<String>> tmpMap = new TreeMap<>(original);
        tmpMap.put(key, immutableCopy(value));
        return tmpMap;
    }

    private static TreeMap<String, List<String>> delete(
            final TreeMap<String, List<String>> original,
            final Collection<String> keys) {

        // This internally uses the comparator of the map
        if (Collections.disjoint(original.keySet(), keys))
            return original;

        TreeMap<String, List<String>> modifiedMap = new TreeMap<>(original);
        keys.forEach(modifiedMap::remove);
        return modifiedMap;
    }

}
