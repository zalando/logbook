package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static com.google.common.collect.Sets.newTreeSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.QueryFilters.defaultValue;
import static org.zalando.logbook.QueryFilters.replaceQuery;

final class QueryFiltersTest {

    @Test
    void accessTokenShouldFilterAccessTokenParameterByDefault() {
        final QueryFilter unit = defaultValue();

        assertThat(unit.filter("name=alice&access_token=bob"),
                is("name=alice&access_token=XXX"));
    }

    @CsvSource({
            "name=Alice,name=XXX",
            "name=Alice&name=Bob,name=XXX&name=XXX",
            "name=Alice&active=true,name=XXX&active=true",
            "active&name=Alice,active&name=XXX",
            "name=Alice&active&age=5,name=XXX&active&age=5",
            "name=Alice&secret=123,name=XXX&secret=XXX",
    })
    @ParameterizedTest
    void shouldReplaceMatchingQueryParameters(
            final String query, final String expected) {

        final Set<String> properties = newTreeSet(String::compareToIgnoreCase);
        properties.add("SECRET");
        properties.add("name");

        final QueryFilter unit = replaceQuery(properties::contains, "XXX");

        assertThat(unit.filter(query), is(expected));
    }

    @CsvSource({
            "q=boots&sort=price&direction=asc,sort=price&direction=asc",
            "sort=price&direction=asc&q=boots,sort=price&direction=asc",
            "sort=price&q=boots&direction=asc,sort=price&direction=asc",
            "sort=price&direction=asc,sort=price&direction=asc",
            "q=boots&test=true&q=boots,test=true",
            "q=1&q=2&q=3,''",
            "'',''"
    })
    @ParameterizedTest
    void shouldRemoveGivenQueryParameters(
            final String query, final String expected) {

        final QueryFilter unit = QueryFilters.removeQuery("q");

        assertThat(unit.filter(query), is(expected));
    }

}
