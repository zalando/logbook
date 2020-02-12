package org.zalando.logbook.json;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;

public final class JsonBodyFilters {

    /*language=RegExp*/
    private static final String BOOLEAN_PATTERN = "(?:true|false)";

    /*language=RegExp*/
    private static final String NUMBER_PATTERN =
            "(?:-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)";

    private static final Pattern NUMBER = pattern(NUMBER_PATTERN);

    /**
     * @see <a href="https://stackoverflow.com/a/43597014/232539">Regex for quoted string with escaping quotes</a>
     */
    /*language=RegExp*/
    private static final String STRING_PATTERN = "(?:\"(.*?[^\\\\])??((\\\\\\\\)+)?+\")";

    private static final Pattern STRING = pattern(STRING_PATTERN);

    /*language=RegExp*/
    private static final String PRIMITIVE_PATTERN =
            "(?:" + BOOLEAN_PATTERN + "|" + NUMBER_PATTERN + "|" + STRING_PATTERN + ")";

    private static final Pattern PRIMITIVE = pattern(PRIMITIVE_PATTERN);

    private JsonBodyFilters() {

    }

    private static Pattern pattern(final String value) {
        return compile("(?<key>\"(?<property>.*?)\"\\s*:\\s*)(" + value + "|null)");
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
    public static BodyFilter replaceJsonStringProperty(
            final Set<String> properties, final String replacement) {

        return replaceJsonStringProperty(properties::contains, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonStringProperty(
            final Predicate<String> predicate, final String replacement) {

        return replace(predicate, STRING, quote(replacement));
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonNumberProperty(
            final Set<String> properties, final Number replacement) {

        final Predicate<String> predicate = properties::contains;
        return replaceJsonNumberProperty(predicate, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonNumberProperty(
            final Predicate<String> predicate, final Number replacement) {

        return replace(predicate, NUMBER, String.valueOf(replacement));
    }

    public static BodyFilter replacePrimitiveJsonProperty(
            final Predicate<String> predicate, final String replacement) {

        return replace(predicate, PRIMITIVE, quote(replacement));
    }

    public static String quote(final String s) {
        return "\"" + s + "\"";
    }

    private static BodyFilter replace(
            final Predicate<String> predicate,
            final Pattern pattern,
            final String replacement) {

        final UnaryOperator<String> delegate = body -> {
            final Matcher matcher = pattern.matcher(body);
            final StringBuffer result = new StringBuffer(body.length());

            while (matcher.find()) {
                if (predicate.test(matcher.group("property"))) {
                    // this preserves whitespaces around properties
                    matcher.appendReplacement(result, "${key}");
                    result.append(replacement);
                } else {
                    matcher.appendReplacement(result, "$0");
                }
            }
            matcher.appendTail(result);

            return result.toString();
        };

        return (contentType, body) ->
                JsonMediaType.JSON.test(contentType) ? delegate.apply(body) : body;
    }

}
