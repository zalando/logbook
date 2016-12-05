package org.zalando.logbook;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public final class BodyFilters {

    BodyFilters() {
        // package private so we can trick code coverage
    }

    public static BodyFilter defaultValue() {
        return accessToken();
    }

    public static BodyFilter accessToken() {
        Set<String> properties = new HashSet<>();
        properties.add("access_token");
        properties.add("open_id");
        properties.add("id_token");
        return replaceJSONProperty(properties, "XXX");
    }

    public static BodyFilter replaceJSONProperty(Set<String> properties, String replacement) {
        String regex = properties.stream()
                .map(Pattern::quote)
                .collect(joining("|"));

        final Predicate<String> json = MediaTypeQuery.compile("application/json", "application/*+json");
        final Pattern pattern = Pattern.compile("(\"(?:" + regex + ")\"\\s*\\:\\s*)\".+?\"");

        return (contentType, body) -> json.test(contentType) ?
                pattern.matcher(body).replaceAll("$1\"" + replacement + "\"") : body;
    }

}
