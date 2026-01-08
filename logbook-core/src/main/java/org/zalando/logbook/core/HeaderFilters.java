package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.HeaderFilter;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.nCopies;
import static java.util.Collections.singleton;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.HttpHeaders.predicate;
import static org.zalando.logbook.core.DefaultFilters.defaultValues;

@API(status = STABLE)
public final class HeaderFilters {

    private HeaderFilters() {

    }

    @API(status = MAINTAINED)
    public static HeaderFilter defaultValue() {
        return defaultValues(HeaderFilter.class).stream()
                .reduce(authorization(), HeaderFilter::merge);
    }

    @API(status = MAINTAINED)
    public static HeaderFilter authorization() {
        return replaceHeaders("Authorization", "XXX");
    }

    public static HeaderFilter replaceHeaders(
            final String name,
            final String replacement) {

        return replaceHeaders(singleton(name), replacement);
    }

    public static HeaderFilter replaceHeaders(
            final Collection<String> names,
            final String replacement) {

        return headers -> headers.apply(names, (name, previous) ->
                previous == null ?
                        null :
                        nCopies(previous.size(), replacement));
    }

    public static HeaderFilter replaceCookies(
            final Predicate<String> predicate, final String replacement) {
        return new CookieHeaderFilter(predicate, cookieValue -> replacement);
    }

    public static HeaderFilter replaceCookies(
            final Predicate<String> predicate,
            final Function<String, String> replacer) {
        return new CookieHeaderFilter(predicate, replacer);
    }

    public static HeaderFilter replaceHeaders(
            final Predicate<String> keyPredicate,
            final String replacement) {

        return replaceHeaders(predicate(keyPredicate), replacement);
    }

    public static HeaderFilter replaceHeaders(
            final BiPredicate<String, String> predicate,
            final String replacement) {

        return eachHeader((key, value) ->
                predicate.test(key, value) ? replacement : value);
    }

    public static HeaderFilter eachHeader(
            final BinaryOperator<String> operator) {

        return headers -> headers.apply(headers.keySet(), (name, values) -> {
            final List<String> result = values.stream()
                    .map(value -> operator.apply(name, value))
                    .toList();

            if (result.equals(values)) {
                // in order not to produce a new version of headers
                return values;
            }

            return result;
        });
    }

    public static HeaderFilter removeHeaders(
            final String... names) {

        return headers -> headers.delete(names);
    }

    public static HeaderFilter removeHeaders(
            final Predicate<String> predicate) {

        return headers -> headers.delete(predicate(predicate));
    }

    public static HeaderFilter removeHeaders(
            final BiPredicate<String, String> predicate) {

        return headers -> headers.apply((name, previous) -> {
            if (previous.isEmpty()) {
                return previous;
            }

            final List<String> next = previous.stream()
                    .filter(value -> predicate.negate().test(name, value))
                    .toList();

            if (next.isEmpty()) {
                return null;
            } else if (next.size() == previous.size()) {
                return previous;
            } else {
                return next;
            }
        });
    }

}
