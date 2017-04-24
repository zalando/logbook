package org.zalando.logbook;

import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

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

            headers.forEach((key, values) ->
                    values.stream()
                            .map(value -> operator.apply(key, value))
                            .forEach(value -> result.put(key, value)));

            return result.build();
        };
    }

    public static HeaderFilter removeHeaders(final Predicate<String> keyPredicate) {
        return removeHeaders((key, value) -> keyPredicate.test(key));
    }

    public static HeaderFilter removeHeaders(final BiPredicate<String, String> predicate) {
        return headers -> {
            final BaseHttpMessage.HeadersBuilder result = new BaseHttpMessage.HeadersBuilder();

            headers.forEach((key, values) ->
                    values.stream()
                            .filter(value -> !predicate.test(key, value))
                            .forEach(value -> result.put(key, value)));

            return result.build();
        };
    }

}
