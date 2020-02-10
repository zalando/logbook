package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.DefaultFilters.defaultValues;

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
        return replaceHeaders("Authorization"::equalsIgnoreCase, "XXX");
    }

    public static HeaderFilter replaceCookies(
            final Predicate<String> predicate, final String replacement) {
        return new CookieHeaderFilter(predicate, replacement);
    }

    public static HeaderFilter replaceHeaders(final Predicate<String> keyPredicate, final String replacement) {
        return eachHeader((key, value) -> keyPredicate.test(key) ? replacement : value);
    }

    public static HeaderFilter replaceHeaders(final BiPredicate<String, String> predicate, final String replacement) {
        return eachHeader((key, value) -> predicate.test(key, value) ? replacement : value);
    }

    public static HeaderFilter eachHeader(final BinaryOperator<String> operator) {
        return headers -> {
            final Map<String, List<String>> result = Headers.empty();

            headers.forEach((name, values) ->
                    result.put(name, values.stream()
                            .map(value -> operator.apply(name, value))
                            .collect(toList())));

            return Headers.immutableCopy(result);
        };
    }

    public static HeaderFilter removeHeaders(final Predicate<String> keyPredicate) {
        return removeHeaders((key, value) -> keyPredicate.test(key));
    }

    public static HeaderFilter removeHeaders(final BiPredicate<String, String> predicate) {
        return headers -> {
            final Map<String, List<String>> result = Headers.empty();

            headers.forEach((name, original) -> {
                final List<String> values = original.stream()
                        .filter(value -> !predicate.test(name, value))
                        .collect(toList());

                if (values.isEmpty()) {
                    return;
                }

                result.put(name, values);
            });

            return Headers.immutableCopy(result);
        };
    }

}
