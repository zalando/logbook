package org.zalando.logbook;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public final class HeaderFilters {

    HeaderFilters() {
        // package private so we can trick code coverage
    }

    public static HeaderFilter defaultValue() {
        return authorization();
    }

    public static HeaderFilter authorization() {
        return replaceHeaders("Authorization"::equalsIgnoreCase, "XXX");
    }

    public static HeaderFilter replaceHeaders(final Predicate<String> keyPredicate, final String replacement) {
        return eachHeader((key, value) -> keyPredicate.test(key) ? replacement : value);
    }

    public static HeaderFilter replaceHeaders(final BiPredicate<String, String> predicate, final String replacement) {
        return eachHeader((key, value) -> predicate.test(key, value) ? replacement : value);
    }

    public static HeaderFilter removeHeaders(final Predicate<String> keyPredicate) {
        return eachHeader((key, value) -> keyPredicate.test(key), (key, value) -> value);
    }

    public static HeaderFilter removeHeaders(final BiPredicate<String, String> predicate) {
        return eachHeader(predicate, (key, value) -> value);
    }

    public static HeaderFilter eachHeader(final BinaryOperator<String> operator) {
        return eachHeader((key, value) -> false, operator);
    }

    public static HeaderFilter eachHeader(final BiPredicate<String, String> remove, final BinaryOperator<String> change) {
        return headers -> {
            final BaseHttpMessage.HeadersBuilder result = new BaseHttpMessage.HeadersBuilder();

            for (final Map.Entry<String, List<String>> e : headers.entrySet()) {
                final String k = e.getKey();
                result.put(k, e.getValue().stream()
                        .filter(v -> !remove.test(k, v))
                        .map(v -> change.apply(k, v))
                        .collect(toList()));
            }

            return result.build();
        };
    }
}
