package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.common.MediaTypeQuery;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.core.DefaultFilters.defaultValues;
import static org.zalando.logbook.core.QueryFilters.replaceQuery;

@API(status = STABLE)
public final class BodyFilters {

    private BodyFilters() {

    }

    @API(status = MAINTAINED)
    public static BodyFilter defaultValue() {
        return defaultValues(BodyFilter.class).stream()
                .reduce(oauthRequest(), BodyFilter::merge);
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter oauthRequest() {
        final Set<String> properties = new HashSet<>();
        properties.add("client_secret");
        properties.add("password");
        return replaceFormUrlEncodedProperty(properties, "XXX");
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

        final QueryFilter delegate =
                replaceQuery(properties::contains, replacement);

        return (contentType, body) -> formUrlEncoded.test(contentType) ? delegate.filter(body) : body;
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter truncate(final int maxSize) {
        return (contentType, body) -> body.length() <= maxSize ? body : body.substring(0, maxSize) + "...";
    }

    @API(status = EXPERIMENTAL)
    public static BodyFilter compactXml() {
        return new CompactingXmlBodyFilter();
    }

}
