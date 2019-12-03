package org.zalando.logbook;

import org.apiguardian.api.API;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.DefaultFilters.defaultValues;

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
            final Predicate<String> predicate, final String replacement) {

        final Pattern pattern = compile("(?<name>[^&]*?)=(?:[^&]*)");

        return query -> {
            final Matcher matcher = pattern.matcher(query);
            final StringBuffer result = new StringBuffer(query.length());

            while (matcher.find()) {
                if (predicate.test(matcher.group("name"))) {
                    matcher.appendReplacement(result, "${name}");
                    result.append('=');
                    result.append(replacement);
                } else {
                    matcher.appendReplacement(result, "$0");
                }
            }
            matcher.appendTail(result);

            return result.toString();
        };
    }

    @API(status = EXPERIMENTAL)
    public static QueryFilter removeQuery(final String name) {
        final Predicate<String> predicate = name::equals;
        final Pattern pattern = compile("&?(?<name>[^&]+?)=(?:[^&]*)");

        return query -> {
            final Matcher matcher = pattern.matcher(query);
            final StringBuffer result = new StringBuffer(query.length());

            while (matcher.find()) {
                if (predicate.test(matcher.group("name"))) {
                    matcher.appendReplacement(result, "");
                } else {
                    matcher.appendReplacement(result, "$0");
                }
            }
            matcher.appendTail(result);

            final String output = result.toString();

            if (output.startsWith("&")) {
                // ideally this case would be covered by the regex, but
                // I wasn't able to make it work
                return output.substring(1);
            }

            return output;
        };
    }

}
