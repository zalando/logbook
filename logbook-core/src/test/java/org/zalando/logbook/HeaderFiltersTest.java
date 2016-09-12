package org.zalando.logbook;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.zalando.logbook.HeaderFilters.defaultValue;
import static org.zalando.logbook.HeaderFilters.eachHeader;

public final class HeaderFiltersTest {

    @Test
    public void shouldFilterHeaders() {
        final Map<String, List<String>> actual = eachHeader((k, v) -> v.toUpperCase())
                .filter(MockHeaders.of("hello", "world", "chao", "bambina"));

        assertEquals(2, actual.size());
        assertTrue(actual.containsKey("hello"));
        assertTrue(actual.containsKey("chao"));
        assertEquals(singletonList("WORLD"), actual.get("hello"));
        assertEquals(singletonList("BAMBINA"), actual.get("chao"));
    }

    @Test
    public void shouldOnlyFilterHeaderIfBothNameAndValueApply() {
        final HeaderFilter unit = HeaderFilters.replaceHeaders((name, value) ->
                "name".equals(name) && "Alice".equals(value), "<secret>");

        assertThat(unit.filter(MockHeaders.of("name", "Alice")), hasEntry("name", singletonList("<secret>")));
        assertThat(unit.filter(MockHeaders.of("name", "Bob")), hasEntry("name", singletonList("Bob")));
    }

    @Test
    public void authorizationShouldFilterAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(unit.filter(MockHeaders.of("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671")),
                hasEntry("Authorization", singletonList("XXX")));
    }

    @Test
    public void authorizationShouldNotFilterNonAuthorizationByDefault() {
        final HeaderFilter unit = defaultValue();

        assertThat(unit.filter(MockHeaders.of("Accept", "text/plain")), hasEntry("Accept", singletonList("text/plain")));
    }

}