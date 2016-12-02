package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class BodyFiltersTest {

    @Test
    public void shouldFilterAccessTokenByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("application/json", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"XXX\"}"));
    }

    @Test
    public void shouldNotFilterAccessTokenInTextPlainByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("text/plain", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"secret\"}"));
    }

    @Test
    public void shouldFilterProperty() {
        final BodyFilter unit = BodyFilters.replaceProperty("foo", "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual, is("{\"foo\":\"XXX\"}"));
    }

}