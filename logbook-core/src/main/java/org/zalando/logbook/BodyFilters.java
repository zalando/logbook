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
        final Set<String> properties = new HashSet<>();
        properties.add("access_token");
        properties.add("open_id");
        properties.add("id_token");
        return replaceJsonStringProperty(properties, "XXX");
    }

    /**
     * Creates a {@link BodyFilter} that replaces the properties in the json response with the replacement passed as argument.
     * This {@link BodyFilter} works on all levels inside the json tree and it only works with string values<br><br>
     * Example from {@link #accessToken} method:<br>
     * <pre>
     * Set<String> properties = new HashSet<>();
     * properties.add("access_token");
     * properties.add("open_id");
     * properties.add("id_token");
     * return replaceJsonStringProperty(properties, "XXX");
     * </pre>
     *
     * @param properties  JSON properties to replace
     * @param replacement String to replace the properties values
     * @return BodyFilter generated
     */
    public static BodyFilter replaceJsonStringProperty(final Set<String> properties, final String replacement) {
        final String regex = properties.stream()
                .map(Pattern::quote)
                .collect(joining("|"));

        final Predicate<String> json = MediaTypeQuery.compile("application/json", "application/*+json");
        final Pattern pattern = Pattern.compile("(\"(?:" + regex + ")\"\\s*\\:\\s*)\".+?\"");

        return (contentType, body) -> json.test(contentType) ?
                pattern.matcher(body).replaceAll("$1\"" + replacement + "\"") : body;
    }

}
