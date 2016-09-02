package org.zalando.logbook;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.zalando.logbook.Filters.filterHeaders;

public final class FiltersTest {

    @Test
    public void accessTokenShouldFilterAccessTokenParameter() {
        final QueryFilter unit = Filters.accessToken();

        assertThat(unit.filter("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

    @Test
    public void shouldOnlyFilterHeaderIfBothNameAndValueApply() {
        final HeaderFilter unit = Filters.obfuscate((name, value) ->
                "name".equals(name) && "Alice".equals(value), "<secret>");

        assertThat(unit.filter("name", "Alice"), is("<secret>"));
        assertThat(unit.filter("name", "Bob"), is("Bob"));
    }

    @Test
    public void authorizationShouldFilterAuthorization() {
        final HeaderFilter unit = Filters.authorization();

        assertThat(unit.filter("Authorization", "Bearer c61a8f84-6834-11e5-a607-10ddb1ee7671"),
                is(equalTo("XXX")));
    }

    @Test
    public void authorizationShouldNotFilterNonAuthorization() {
        final HeaderFilter unit = Filters.authorization();

        assertThat(unit.filter("Accept", "text/plain"), is(equalTo("text/plain")));
    }

    @Test
    public void shouldFilterHeaders() {
        final Map<String, List<String>> m = filterHeaders(
            MockHeaders.of("hello", "world", "chao", "bambina"),
            (k, v) -> v.toUpperCase()
        );

        assertEquals(2, m.size());
        assertTrue(m.containsKey("hello"));
        assertTrue(m.containsKey("chao"));
        assertEquals(singletonList("WORLD"), m.get("hello"));
        assertEquals(singletonList("BAMBINA"), m.get("chao"));
    }
}
