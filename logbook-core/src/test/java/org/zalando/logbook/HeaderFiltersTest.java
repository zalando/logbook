package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.zalando.logbook.HeaderFilters.defaultValue;
import static org.zalando.logbook.HeaderFilters.eachHeader;
import static org.zalando.logbook.HeaderFilters.removeHeaders;

final class HeaderFiltersTest {

    @Test
    void appliesToEachHeader() {
        final HeaderFilter unit = eachHeader((k, v) -> v.toUpperCase());
        final HttpHeaders actual = unit.filter(
                HttpHeaders.empty()
                        .update("hello", "world")
                        .update("chao", "bambina")
                        .update("already", "UPPERCASE"));

        final HttpHeaders expected = HttpHeaders.empty()
                .update("hello", "WORLD")
                .update("chao", "BAMBINA")
                .update("already", "UPPERCASE");

        assertEquals(expected, actual);
    }

    @Test
    void replacesHeadersByName() {
        final HeaderFilter unit = HeaderFilters.replaceHeaders(
                "name"::equalsIgnoreCase,
                "<secret>");

        final HttpHeaders actual = unit.filter(HttpHeaders.empty()
                .update("name", "Alice", "Bob"));

        assertThat(actual,
                hasEntry("name", Arrays.asList("<secret>", "<secret>")));
    }

    @Test
    void replacesHeadersByNameAndValue() {
        final HeaderFilter unit = HeaderFilters.replaceHeaders(
                (name, value) -> "name".equals(name) && "Alice".equals(value),
                "<secret>");

        assertThat(
                unit.filter(HttpHeaders.of("name", "Alice")),
                hasEntry("name", singletonList("<secret>")));

        assertThat(
                unit.filter(HttpHeaders.of("name", "Bob")),
                hasEntry("name", singletonList("Bob")));
    }

    @Test
    void authorizationShouldFilterAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(
                unit.filter(HttpHeaders.of("Authorization",
                                "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671",
                                "Basic dXNlcjpwYXNzd29yZA==")),
                hasEntry("Authorization", Arrays.asList("XXX", "XXX")));
    }

    @Test
    void authorizationShouldNotFilterNonAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(
                unit.filter(HttpHeaders.of("Accept", "text/plain")),
                hasEntry("Accept", singletonList("text/plain")));
    }

    @Test
    void shouldRemoveHeaderByName() {
        final HeaderFilter unit = removeHeaders("name");

        final HttpHeaders filtered = unit.filter(
                HttpHeaders.of("name", "Alice", "Bob"));

        assertThat(filtered, not(hasKey("name")));
    }

    @Test
    void shouldRemoveHeaderByNamePredicate() {
        final HeaderFilter unit = removeHeaders("name"::equals);

        final HttpHeaders filtered = unit.filter(
                HttpHeaders.empty()
                        .update("name", "Alice", "Bob")
                        .update("age", "18"));

        assertThat(filtered, not(hasKey("name")));
        assertThat(filtered, hasEntry("age", singletonList("18")));
    }

    @Test
    void shouldRemoveHeaderByNameAndValue() {
        final HeaderFilter unit = removeHeaders((name, value) ->
                "name".equals(name) && "Alice".equals(value));

        final HttpHeaders filtered = unit.filter(
                HttpHeaders.of("name", "Alice", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, hasEntry("name", singletonList("Bob")));
    }

    @Test
    void shouldRemoveHeaderByValue() {
        final HeaderFilter unit = removeHeaders(
                (name, value) -> "Alice".equals(value));

        final HttpHeaders filtered = unit.filter(
                HttpHeaders.of("name", "Alice", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, hasEntry("name", singletonList("Bob")));
    }

    @Test
    void shouldRemoveAndChangeHeader() {
        final HeaderFilter unit = HeaderFilter.merge(
                removeHeaders((key, value) ->
                        "name".equals(key) && "Bob".equals(value)),
                eachHeader((name, value) ->
                        "name".equals(name) && "Alice".equals(value) ? "Carol" : value));

        final HttpHeaders filtered = unit.filter(
                HttpHeaders.of("name", "Alice", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, not(hasEntry("name", singletonList("Bob"))));
        assertThat(filtered, hasEntry("name", singletonList("Carol")));
    }

    @Test
    void removesWholeHeader() {
        final HeaderFilter unit = removeHeaders((name, value) ->
                "Set-Cookie".equals(name));

        final HttpHeaders actual = unit.filter(
                HttpHeaders.of("Set-Cookie", "version=1", "user=me"));

        assertEquals(actual, HttpHeaders.empty());
    }

    @Test
    void applyLeavesAlreadyEmptyListUntouched() {
        final HeaderFilter unit = removeHeaders((name, value) ->
                "Set-Cookie".equals(name));

        final HttpHeaders headers = HttpHeaders.of("Set-Cookie");

        final HttpHeaders actual = unit.filter(headers);

        assertSame(actual, headers);
        assertFalse(actual.isEmpty());
    }

    @Test
    void applyLeavesHeadersUntouched() {
        final HeaderFilter unit = removeHeaders((name, value) ->
                "Set-Cookie".equals(name) && "version=2".equals(value));

        final HttpHeaders headers = HttpHeaders.of(
                "Set-Cookie", "version=1", "user=me");

        final HttpHeaders actual = unit.filter(headers);

        assertSame(headers, actual);
    }

}
