package org.zalando.logbook;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.zalando.logbook.DefaultHttpHeaders.EMPTY;

/**
 * An immutable multi-map representing HTTP headers. It offers three kinds of
 * operations:
 *
 * {@code update} is essentially {@link Map#put(Object, Object)}, but returns
 * a new instance with the updated entries.
 *
 * {@code delete} is essentially {@link Map#remove(Object)}, but returns a new
 * instance without the deleted headers.
 *
 * {@code apply} applies a function to all entries or a subset of them and
 * applies the changes. If the given operator returns a null value the entry
 * will be deleted otherwise updated.
 */
@Immutable
public interface HttpHeaders extends Map<String, List<String>> {

    @CheckReturnValue
    HttpHeaders update(String name, String... values);

    @CheckReturnValue
    HttpHeaders update(String name, Collection<String> value);

    @CheckReturnValue
    HttpHeaders update(Map<String, List<String>> headers);

    @CheckReturnValue
    HttpHeaders apply(String name, UnaryOperator<List<String>> operator);

    @CheckReturnValue
    HttpHeaders apply(
            Collection<String> names,
            BiFunction<String, List<String>, Collection<String>> operator);

    @CheckReturnValue
    HttpHeaders apply(
            BiPredicate<String, List<String>> predicate,
            BiFunction<String, List<String>, Collection<String>> operator);

    @CheckReturnValue
    HttpHeaders apply(
            BiFunction<String, List<String>, Collection<String>> operator);

    @CheckReturnValue
    HttpHeaders delete(String... names);

    @CheckReturnValue
    HttpHeaders delete(Collection<String> names);

    @CheckReturnValue
    HttpHeaders delete(BiPredicate<String, List<String>> predicate);

    static HttpHeaders empty() {
        return EMPTY;
    }

    static HttpHeaders of(final String name, final String... values) {
        return empty().update(name, values);
    }

    static HttpHeaders of(final Map<String, List<String>> headers) {
        return empty().update(headers);
    }

    static <T, U> BiPredicate<T, U> predicate(final Predicate<T> predicate) {
        return (t, u) -> predicate.test(t);
    }

    // deprecated stuff from here on till the end

    @Deprecated
    @Override
    List<String> put(String key, List<String> value);

    @Deprecated
    @Override
    List<String> remove(Object key);

    @Deprecated
    @Override
    void putAll(Map<? extends String, ? extends List<String>> m);

    @Deprecated
    @Override
    void clear();

    @Deprecated
    @Override
    void replaceAll(BiFunction<? super String, ? super List<String>, ? extends List<String>> function);

    @Deprecated
    @Override
    List<String> putIfAbsent(String key, List<String> value);

    @Deprecated
    @Override
    boolean remove(Object key, Object value);

    @Deprecated
    @Override
    boolean replace(String key, List<String> oldValue, List<String> newValue);

    @Deprecated
    @Override
    List<String> replace(String key, List<String> value);

    @Deprecated
    @Override
    List<String> computeIfAbsent(String key, Function<? super String, ? extends List<String>> mappingFunction);

    @Deprecated
    @Override
    List<String> computeIfPresent(String key,
            BiFunction<? super String, ? super List<String>, ? extends List<String>> remappingFunction);

    @Deprecated
    @Override
    List<String> compute(String key,
            BiFunction<? super String, ? super List<String>, ? extends List<String>> remappingFunction);

    @Deprecated
    @Override
    List<String> merge(String key, List<String> value,
            BiFunction<? super List<String>, ? super List<String>, ? extends List<String>> remappingFunction);

}
