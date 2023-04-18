package org.zalando.logbook.api;

import lombok.AllArgsConstructor;
import lombok.With;
import lombok.experimental.Delegate;
import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImSortedMap;
import org.organicdesign.fp.collections.PersistentTreeMap;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;
import static org.organicdesign.fp.collections.PersistentVector.ofIter;
import static org.zalando.logbook.api.Fold.fold;

@SuppressWarnings("deprecation") // needed because of @Delegate and @Deprecated
@AllArgsConstructor(access = PRIVATE)
final class DefaultHttpHeaders
        // gives us a meaningful equals, hashCode and toString
        extends AbstractMap<String, List<String>>
        implements UpdateHttpHeaders, ApplyHttpHeaders, DeleteHttpHeaders {

    static final HttpHeaders EMPTY = new DefaultHttpHeaders();

    @With
    private final ImSortedMap<String, List<String>> headers;

    private DefaultHttpHeaders() {
        this(PersistentTreeMap.empty(String.CASE_INSENSITIVE_ORDER));
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

        return withHeaders(
                headers.assoc(name, immutableCopy(values)));
    }

    @Override
    public HttpHeaders delete(final Collection<String> names) {
        return withHeaders(
                fold(names, headers, ImSortedMap::without));
    }

    public static <T> List<T> immutableCopy(final Collection<T> values) {
        if (values instanceof ImListWithToString) {
            return (List<T>) values;
        }
        return new ImListWithToString<>(ofIter(values));
    }

    @AllArgsConstructor
    private static final class ImListWithToString<T> extends AbstractList<T> {

        private final ImList<T> list;

        @Delegate
        @SuppressWarnings("unused")
        private List<T> delegate() {
            return list;
        }

    }

}
