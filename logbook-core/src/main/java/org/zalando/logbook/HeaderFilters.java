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

    public static HeaderFilter eachHeader(final BinaryOperator<String> operator) {
        return headers -> {
            final BaseHttpMessage.HeadersBuilder result = new BaseHttpMessage.HeadersBuilder();

            for (final Map.Entry<String, List<String>> e : headers.entrySet()) {
                final String k = e.getKey();
                result.put(k, e.getValue().stream().map(x -> operator.apply(k, x)).collect(toList()));
            }

            return result.build();
        };
    }

}
