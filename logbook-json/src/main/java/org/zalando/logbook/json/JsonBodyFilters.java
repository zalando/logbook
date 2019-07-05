package org.zalando.logbook.json;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;

public final class JsonBodyFilters {

    private JsonBodyFilters() {

    }

    @API(status = MAINTAINED)
    public static BodyFilter accessToken() {
        final Set<String> properties = new HashSet<>(Arrays.asList(
                "access_token", "refresh_token", "open_id", "id_token"));
        return replaceJsonStringProperty(properties, "XXX");
    }


    /**
     * Creates a {@link BodyFilter} that replaces the properties in the json response with the replacement passed as argument.
     * This {@link BodyFilter} works on all levels inside the json tree and it only works with string values<br><br>
     * Example from {@link AccessTokenBodyFilter} method:<br>
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
        /*language=RegExp*/
        final String string = "(?:\"(?:[^\"\\\\]|\\\\.)*\")";
        return replacePrimitiveJsonProperty(properties, string, "\"" + replacement + "\"");
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonNumberProperty(final Set<String> properties, final Number replacement) {
        /*language=RegExp*/
        final String number = "(?:-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)";
        return replacePrimitiveJsonProperty(properties, number, String.valueOf(replacement));
    }

    private static BodyFilter replacePrimitiveJsonProperty(final Set<String> properties, final String value, final String replacement) {
        final String regex = properties.stream().map(Pattern::quote).collect(joining("|"));
        final String property = "\"(?:" + regex + ")\"";
        final Pattern pattern = compile("(" + property + "\\s*:\\s*)" + value + "|null");
        final UnaryOperator<String> delegate = body -> pattern.matcher(body).replaceAll("$1" + replacement);

        return (contentType, body) ->
                JsonMediaType.JSON.test(contentType) ? delegate.apply(body) : body;
    }

}
