package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apiguardian.api.API;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class BodyFilters {

    private BodyFilters() {

    }

    @API(status = MAINTAINED)
    public static BodyFilter defaultValue() {
        return accessToken();
    }

    @API(status = MAINTAINED)
    public static BodyFilter accessToken() {
        final Set<String> properties = new HashSet<>();
        properties.add("access_token");
        properties.add("refresh_token");
        properties.add("open_id");
        properties.add("id_token");
        return replaceJsonStringProperty(properties, "XXX");
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter oauthRequest() {
        final Set<String> properties = new HashSet<>();
        properties.add("client_secret");
        properties.add("password");
        return replaceFormUrlEncodedProperty(properties, "XXX");
    }

    /**
     * Creates a {@link BodyFilter} that replaces the properties in the json response with the replacement passed as argument.
     * This {@link BodyFilter} works on all levels inside the json tree and it only works with string values<br><br>
     * Example from {@link #accessToken} method:<br>
     * <pre>{@code
     * Set<String> properties = new HashSet<>();
     * properties.add("access_token");
     * properties.add("open_id");
     * properties.add("id_token");
     * return replaceJsonStringProperty(properties, "XXX");
     * }</pre>
     *
     * @param properties  JSON properties to replace
     * @param replacement String to replace the properties values
     * @return BodyFilter generated
     */
    @API(status = MAINTAINED)
    public static BodyFilter replaceJsonStringProperty(final Set<String> properties, final String replacement) {
        final Predicate<String> json = MediaTypeQuery.compile("application/json", "application/*+json");

        final String regex = properties.stream().map(Pattern::quote).collect(joining("|"));
        final Pattern pattern = Pattern.compile("(\"(?:" + regex + ")\"\\s*:\\s*)\".*?\"");
        final UnaryOperator<String> delegate = body -> pattern.matcher(body).replaceAll("$1\"" + replacement + "\"");

        return (contentType, body) -> json.test(contentType) ? delegate.apply(body) : body;
    }

    /**
     * Creates a {@link BodyFilter} that replaces the properties in the form url encoded body with given replacement.
     *
     * @param properties  query names properties to replace
     * @param replacement String to replace the properties values
     * @return BodyFilter generated
     */
    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceFormUrlEncodedProperty(final Set<String> properties, final String replacement) {
        final Predicate<String> formUrlEncoded = MediaTypeQuery.compile("application/x-www-form-urlencoded");

        final QueryFilter delegate = properties.stream()
                .map(name -> QueryFilters.replaceQuery(name, replacement))
                .reduce(QueryFilter::merge)
                .orElseGet(QueryFilter::none);

        return (contentType, body) -> formUrlEncoded.test(contentType) ? delegate.filter(body) : body;
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter truncate(final int maxSize) {
        return (contentType, body) -> body.length() <= maxSize ? body : body.substring(0, maxSize) + "...";
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter compactJson(final ObjectMapper objectMapper) {
        return new JsonCompactingBodyFilter(objectMapper);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter compactXml() {
        return new XmlCompactingBodyFilter();
    }

}
