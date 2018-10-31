package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class HeaderFilters {

    private HeaderFilters() {

    }

    @API(status = MAINTAINED)
    public static HeaderFilter defaultValue() {
        return authorization();
    }

    @API(status = MAINTAINED)
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
            final HttpMessage.HeadersBuilder result = new HttpMessage.HeadersBuilder();

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
            final HttpMessage.HeadersBuilder result = new HttpMessage.HeadersBuilder();

            headers.forEach((key, values) ->
                    values.stream()
                            .filter(value -> !predicate.test(key, value))
                            .forEach(value -> result.put(key, value)));

            return result.build();
        };
    }

}
