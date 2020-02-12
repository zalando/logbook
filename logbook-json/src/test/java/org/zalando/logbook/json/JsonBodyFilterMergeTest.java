package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonNumberProperty;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

final class JsonBodyFilterMergeTest {

    @Test
    void mergesPrimitiveJsonPropertyFilters() {
        final BodyFilter unit = BodyFilter.merge(
                replaceJsonStringProperty("secret"::equals, "XXX"),
                replaceJsonStringProperty("password"::equals, "XXX"));

        assertThat(unit, is(instanceOf(PrimitiveJsonPropertyBodyFilter.class)));

        final String actual = unit.filter("application/json",
                "{\"secret\":\"abc\",\"password\":\"123\"}");

        assertThat(actual, is("{\"secret\":\"XXX\",\"password\":\"XXX\"}"));
    }

    @Test
    void wontMergePrimitiveJsonPropertyFilterWithDifferentReplacements() {
        final BodyFilter unit = BodyFilter.merge(
                replaceJsonStringProperty("secret"::equals, "XXX"),
                replaceJsonStringProperty("password"::equals, "xxx"));

        assertThat(unit,
                is(not(instanceOf(PrimitiveJsonPropertyBodyFilter.class))));
    }

    @Test
    void wontMergePrimitiveJsonPropertyFilterWithPropertyTypes() {
        final BodyFilter unit = BodyFilter.merge(
                replaceJsonStringProperty("secret"::equals, "XXX"),
                replaceJsonNumberProperty("age"::equals, 123));

        assertThat(unit,
                is(not(instanceOf(PrimitiveJsonPropertyBodyFilter.class))));
    }

    @Test
    void wontMergePrimitiveJsonPropertyFilterWithNonPrimitiveJsonPropertyFilter() {
        final BodyFilter unit = BodyFilter.merge(
                replaceJsonStringProperty("secret"::equals, "XXX"),
                new CompactingJsonBodyFilter());

        assertThat(unit,
                is(not(instanceOf(PrimitiveJsonPropertyBodyFilter.class))));

    }

}
