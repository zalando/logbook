package org.zalando.logbook;

import lombok.AllArgsConstructor;
import lombok.With;
import lombok.experimental.Delegate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        TreeMap<String, List<String>> updatedHeaders = associate(headers, name, immutableCopy(values));
        return headers.equals(updatedHeaders) ? this : withHeaders(updatedHeaders);
    }

    @Override
    public HttpHeaders delete(final Collection<String> names) {
        TreeMap<String, List<String>> updatedHeaders = delete(headers, names);
        return headers.equals(updatedHeaders) ? this : withHeaders(updatedHeaders);
    }

    public static <T> List<T> immutableCopy(final Collection<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static <K, V> TreeMap<K, V> associate(final TreeMap<K, V> original, final K key, final V value) {
        Comparator<? super K> cmp = original.comparator();
        Stream<Map.Entry<K, V>> tmpStream = Stream.of(new SimpleEntry<>(key, value));

        return Stream.concat(original.entrySet().stream(), tmpStream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> cmp.compare(key, entry.getKey()) == 0 ? value : entry.getValue(),
                        (v1, v2) -> v2,
                        () -> new TreeMap<>(cmp)
                ));
    }

    private static <K, V> TreeMap<K, V> delete(final TreeMap<K, V> original, final Collection<K> keys) {
        Comparator<? super K> cmp = original.comparator();
        TreeMap<K, V> modifiedMap = new TreeMap<>(original);
        modifiedMap.keySet().removeIf(k -> keys.stream().anyMatch(key -> cmp.compare(k, key) == 0));
        return modifiedMap;
    }

}
