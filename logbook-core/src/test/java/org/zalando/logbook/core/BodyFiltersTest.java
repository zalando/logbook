package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.logbook.BodyFilter;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.core.BodyFilters.defaultValue;
import static org.zalando.logbook.core.BodyFilters.oauthRequest;
import static org.zalando.logbook.core.BodyFilters.replaceFormUrlEncodedProperty;
import static org.zalando.logbook.core.BodyFilters.truncate;

final class BodyFiltersTest {

    @ParameterizedTest
    @ValueSource(strings = {"client_secret", "password", "refresh_token"})
    void filtersParameterByOauthRequestFilterByDefault(String parameterName) {
        final BodyFilter unit = defaultValue();

        final String actual = unit.filter("application/x-www-form-urlencoded", parameterName + "=secret");

        assertThat(actual).isEqualTo(parameterName + "=XXX");
    }

    @Test
    void wontFilterClientSecretInTextPlainByOauthRequestFilter() {
        final BodyFilter unit = oauthRequest();

        final String actual = unit.filter("text/plain", "client_secret=secret");

        assertThat(actual).isEqualTo("client_secret=secret");
    }

    @Test
    void shouldTruncateBodyIfTooLong() {
        final BodyFilter unit = truncate(5);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual).isEqualTo("{\"foo...");
    }

    @Test
    void shouldNotTruncateBodyIfTooShort() {
        final BodyFilter unit = truncate(50);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"secret\"}");
    }

    @Test
    void shouldFilterFormUrlEncodedBodyIfValidRequest() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(singleton("q"), "XXX");

        final String contentType = "application/x-www-form-urlencoded";
        assertThat(unit.filter(contentType, "q=boots&sort=price&direction=asc")).isEqualTo("q=XXX&sort=price&direction=asc");
        assertThat(unit.filter(contentType, "sort=price&direction=asc&q=boots")).isEqualTo("sort=price&direction=asc&q=XXX");
        assertThat(unit.filter(contentType, "sort=price&q=boots&direction=asc")).isEqualTo("sort=price&q=XXX&direction=asc");
        assertThat(unit.filter(contentType, "sort=price&direction=asc")).isEqualTo("sort=price&direction=asc");
        assertThat(unit.filter(contentType, "q=boots")).isEqualTo("q=XXX");
        assertThat(unit.filter(contentType, "")).isEqualTo("");
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContentType() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(singleton("q"), "XXX");

        assertThat(unit.filter("application/json", "{\"q\":\"boots\"}")).isEqualTo("{\"q\":\"boots\"}");
        assertThat(unit.filter("application/xml", "<q>boots</q>")).isEqualTo("<q>boots</q>");
        assertThat(unit.filter("invalid", "{\"q\":\"boots\"}")).isEqualTo("{\"q\":\"boots\"}");
        assertThat(unit.filter(null, "{\"q\":\"boots\"}")).isEqualTo("{\"q\":\"boots\"}");
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContent() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(singleton("q"), "XXX");

        final String contentType = "application/x-www-form-urlencoded";
        assertThat(unit.filter(contentType, "{\"q\":\"boots\"}")).isEqualTo("{\"q\":\"boots\"}");
        assertThat(unit.filter(contentType, "<q>boots</q>")).isEqualTo("<q>boots</q>");
    }

}
