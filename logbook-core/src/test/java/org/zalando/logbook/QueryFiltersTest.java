package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.QueryFilters.defaultValue;

final class QueryFiltersTest {

    @Test
    void accessTokenShouldFilterAccessTokenParameterByDefault() {
        final QueryFilter unit = defaultValue();

        assertThat(unit.filter("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

    @Test
    void shouldRemoveGivenQueryParameters() {
        final QueryFilter unit = QueryFilters.removeQuery("q");

        assertThat(unit.filter("q=boots&sort=price&direction=asc"), is("sort=price&direction=asc"));
        assertThat(unit.filter("sort=price&direction=asc&q=boots"), is("sort=price&direction=asc"));
        assertThat(unit.filter("sort=price&q=boots&direction=asc"), is("sort=price&direction=asc"));
        assertThat(unit.filter("sort=price&direction=asc"), is("sort=price&direction=asc"));
        assertThat(unit.filter("q=boots"), is(""));
        assertThat(unit.filter(""), is(""));
    }
}
