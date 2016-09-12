package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.QueryFilters.accessToken;
import static org.zalando.logbook.QueryFilters.defaultValue;

public final class QueryFiltersTest {

    @Test
    public void accessTokenShouldFilterAccessTokenParameterByDefault() {
        final QueryFilter unit = defaultValue();

        assertThat(unit.filter("name=alice&access_token=bob"), is(equalTo("name=alice&access_token=XXX")));
    }

}