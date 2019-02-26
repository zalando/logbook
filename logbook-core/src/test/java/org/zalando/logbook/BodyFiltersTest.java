package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.BodyFilters.oauthRequest;
import static org.zalando.logbook.BodyFilters.replaceFormUrlEncodedProperty;
import static org.zalando.logbook.BodyFilters.replaceJsonStringProperty;
import static org.zalando.logbook.BodyFilters.truncate;

final class BodyFiltersTest {

    @Test
    void shouldFilterAccessTokenByDefault() {
        final BodyFilter unit = defaultValue();

        final String actual = unit.filter("application/json", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"XXX\"}"));
    }

    @Test
    void shouldNotFilterAccessTokenInTextPlainByDefault() {
        final BodyFilter unit = defaultValue();

        final String actual = unit.filter("text/plain", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"secret\"}"));
    }

    @Test
    void shouldFilterClientSecretByOauthRequestFilter() {
        final BodyFilter unit = oauthRequest();

        final String actual = unit.filter("application/x-www-form-urlencoded", "client_secret=secret");

        assertThat(actual, is("client_secret=XXX"));
    }

    @Test
    void shouldNotFilterClientSecretInTextPlainByOauthRequestFilter() {
        final BodyFilter unit = oauthRequest();

        final String actual = unit.filter("text/plain", "client_secret=secret");

        assertThat(actual, is("client_secret=secret"));
    }

    @Test
    void shouldFilterNotEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":\"\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldNotFilterNullJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":null,\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":null,\"bar\":\"public\"}"));
    }

    @Test
    void shouldTruncateBodyIfTooLong() {
        final BodyFilter unit = truncate(5);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual, is("{\"foo..."));
    }

    @Test
    void shouldNotTruncateBodyIfTooShort() {
        final BodyFilter unit = truncate(50);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual, is("{\"foo\":\"secret\"}"));
    }

    @Test
    void shouldFilterFormUrlEncodedBodyIfValidRequest() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        final String contentType = "application/x-www-form-urlencoded";
        assertThat(unit.filter(contentType, "q=boots&sort=price&direction=asc"), is("q=XXX&sort=price&direction=asc"));
        assertThat(unit.filter(contentType, "sort=price&direction=asc&q=boots"), is("sort=price&direction=asc&q=XXX"));
        assertThat(unit.filter(contentType, "sort=price&q=boots&direction=asc"), is("sort=price&q=XXX&direction=asc"));
        assertThat(unit.filter(contentType, "sort=price&direction=asc"), is("sort=price&direction=asc"));
        assertThat(unit.filter(contentType, "q=boots"), is("q=XXX"));
        assertThat(unit.filter(contentType, ""), is(""));
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContentType() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        assertThat(unit.filter("application/json", "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(unit.filter("application/xml", "<q>boots</q>"), is("<q>boots</q>"));
        assertThat(unit.filter("invalid", "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(unit.filter(null, "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContent() {
        final BodyFilter unit = replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        final String contentType = "application/x-www-form-urlencoded";
        assertThat(unit.filter(contentType, "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(unit.filter(contentType, "<q>boots</q>"), is("<q>boots</q>"));
    }

}
