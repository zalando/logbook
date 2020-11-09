package org.zalando.logbook;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public class PathFilters {

    private PathFilters() {
    }

    public static PathFilter defaultValue() {
        return PathFilter.none();
    }

    /**
     * Filter/replace by array matching. Accepts filters on the form
     * {@code /myApp/orders/{secret}/order}.
     *
     * <br>
     * <p>
     * Where {@code secret} (including curly braces) gets replaced by the passed replacement.
     *
     * @param expression  filter expression
     * @param replacement value to insert for filtered segments
     * @return a {@link PathFilter} that replaces segments
     */
    @API(status = EXPERIMENTAL)
    public static PathFilter replace(final String expression, final String replacement) {
        return new DefaultPathFilter(replacement, expression);
    }

}
