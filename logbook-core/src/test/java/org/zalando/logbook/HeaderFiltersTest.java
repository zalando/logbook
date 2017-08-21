package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zalando.logbook.HeaderFilters.defaultValue;
import static org.zalando.logbook.HeaderFilters.eachHeader;
import static org.zalando.logbook.HeaderFilters.removeHeaders;

public final class HeaderFiltersTest {

    @Test
    void shouldFilterHeaders() {
        final Map<String, List<String>> actual = eachHeader((k, v) -> v.toUpperCase())
                .filter(MockHeaders.of("hello", "world", "chao", "bambina"));

        assertEquals(2, actual.size());
        assertTrue(actual.containsKey("hello"));
        assertTrue(actual.containsKey("chao"));
        assertEquals(singletonList("WORLD"), actual.get("hello"));
        assertEquals(singletonList("BAMBINA"), actual.get("chao"));
    }

    @Test
    void shouldOnlyFilterHeaderIfBothNameAndValueApply() {
        final HeaderFilter unit = HeaderFilters.replaceHeaders((name, value) ->
                "name".equals(name) && "Alice".equals(value), "<secret>");

        assertThat(unit.filter(MockHeaders.of("name", "Alice")), hasEntry("name", singletonList("<secret>")));
        assertThat(unit.filter(MockHeaders.of("name", "Bob")), hasEntry("name", singletonList("Bob")));
    }

    @Test
    void authorizationShouldFilterAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(unit.filter(MockHeaders.of("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671")),
                hasEntry("Authorization", singletonList("XXX")));
    }

    @Test
    void authorizationShouldNotFilterNonAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(unit.filter(MockHeaders.of("Accept", "text/plain")),
                hasEntry("Accept", singletonList("text/plain")));
    }

    @Test
    void shouldRemoveHeaderByNameAndValue() {
        final HeaderFilter unit = HeaderFilters.removeHeaders((name, value) ->
                "name".equals(name) && "Alice".equals(value));

        final Map<String, List<String>> filtered = unit.filter(MockHeaders.of("name", "Alice", "name", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, hasEntry("name", singletonList("Bob")));
    }

    @Test
    void shouldRemoveHeaderByName() {
        final HeaderFilter unit = HeaderFilters.removeHeaders((name, value) -> "name".equals(name));

        final Map<String, List<String>> filtered = unit.filter(MockHeaders.of("name", "Alice", "name", "Bob"));

        assertThat(filtered, not(hasKey("name")));
    }

    @Test
    void shouldRemoveHeaderByNamePredicate() {
        final HeaderFilter unit = HeaderFilters.removeHeaders("name"::equals);

        final Map<String, List<String>> filtered = unit.filter(
                MockHeaders.of("name", "Alice", "name", "Bob", "age", "18"));

        assertThat(filtered, not(hasKey("name")));
        assertThat(filtered, hasEntry("age", singletonList("18")));
    }

    @Test
    void shouldRemoveHeaderByValue() {
        final HeaderFilter unit = HeaderFilters.removeHeaders((name, value) -> "Alice".equals(value));

        final Map<String, List<String>> filtered = unit.filter(MockHeaders.of("name", "Alice", "name", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, hasEntry("name", singletonList("Bob")));
    }

    @Test
    void shouldRemoveAndChangeHeader() {
        final HeaderFilter unit = HeaderFilter.merge(
                removeHeaders((key, value) -> "name".equals(key) && "Bob".equals(value)),
                eachHeader((name, value) -> "name".equals(name) && "Alice".equals(value) ? "Carol" : value));

        final Map<String, List<String>> filtered = unit.filter(MockHeaders.of("name", "Alice", "name", "Bob"));

        assertThat(filtered, not(hasEntry("name", singletonList("Alice"))));
        assertThat(filtered, not(hasEntry("name", singletonList("Bob"))));
        assertThat(filtered, hasEntry("name", singletonList("Carol")));
    }

}
