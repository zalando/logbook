package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.QueryFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.core.DefaultFilters.defaultValues;

@API(status = STABLE)
public final class QueryFilters {

    private QueryFilters() {
    }

    @API(status = MAINTAINED)
    public static QueryFilter defaultValue() {
        return defaultValues(QueryFilter.class).stream()
                .reduce(accessToken(), QueryFilter::merge);
    }

    @API(status = MAINTAINED)
    public static QueryFilter accessToken() {
        return replaceQuery("access_token", "XXX");
    }

    @API(status = MAINTAINED)
    public static QueryFilter replaceQuery(
            final String name, final String replacement) {

        return replaceQuery(name::equals, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static QueryFilter replaceQuery(
            final String name, final UnaryOperator<String> replacementFunction) {

        return replaceQuery(name::equals, replacementFunction);
    }

    @API(status = EXPERIMENTAL)
    public static QueryFilter replaceQuery(
            final Predicate<String> predicate, final String replacement) {

        return replaceQuery(predicate, s -> replacement);
    }

    @API(status = EXPERIMENTAL)
    public static QueryFilter replaceQuery(
            final Predicate<String> predicate, final UnaryOperator<String> replacementFunction) {

        return query -> processParsedQueryParams(query, (String paramName, String paramValue) -> {
            if (paramValue == null) {
                return paramName;
            } else {
                String newValue = predicate.test(paramName) ? replacementFunction.apply(paramValue) : paramValue;

                return paramName + "=" + newValue;
            }
        });
    }

    @API(status = EXPERIMENTAL)
    public static QueryFilter removeQuery(final String name) {
        final Predicate<String> predicate = name::equals;

        return query -> processParsedQueryParams(query, (String paramName, String paramValue) -> {
            if (predicate.test(paramName)) {
                return null; // indicate removal
            } else {
                return paramName + "=" + paramValue;
            }
        });
    }

    private static String processParsedQueryParams(String query, BiFunction<String, String, String> nameValueHandler) {
        final List<String> result = new ArrayList<>();

        // see https://url.spec.whatwg.org/#urlencoded-parsing
        StringTokenizer tokenizer = new StringTokenizer(query, "&");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int equalsIndex = token.indexOf('=');

            // a token does not always contain an '=', if not the token is the name
            String newParam;
            if (equalsIndex == -1) {
                newParam = nameValueHandler.apply(token, null);
            } else {
                String name = token.substring(0, equalsIndex);
                String value = token.substring(equalsIndex + 1);
                newParam = nameValueHandler.apply(name, value);
            }

            if (newParam != null) {
                result.add(newParam);
            }
        }

        return String.join("&", result);
    }
}
