package org.zalando.logbook.json;

import org.apiguardian.api.API;
import org.zalando.logbook.api.BodyFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.zalando.logbook.json.PrimitiveJsonPropertyBodyFilter.replaceNumber;
import static org.zalando.logbook.json.PrimitiveJsonPropertyBodyFilter.replacePrimitive;
import static org.zalando.logbook.json.PrimitiveJsonPropertyBodyFilter.replaceString;

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
    public static BodyFilter replaceJsonStringProperty(
            final Set<String> properties, final String replacement) {

        return replaceJsonStringProperty(properties::contains, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonStringProperty(
            final Predicate<String> predicate, final String replacement) {

        return replaceString(predicate, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonNumberProperty(
            final Set<String> properties, final Number replacement) {

        return replaceJsonNumberProperty(properties::contains, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replaceJsonNumberProperty(
            final Predicate<String> predicate, final Number replacement) {

        return replaceNumber(predicate, replacement);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter replacePrimitiveJsonProperty(
            final Predicate<String> predicate, final String replacement) {

        return replacePrimitive(predicate, replacement);
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static BodyFilter replacePrimitiveJsonProperty(
            final Predicate<String> predicate, final BiFunction<String, String, String> replacement) {

        return replacePrimitive(predicate, replacement);
    }
}
