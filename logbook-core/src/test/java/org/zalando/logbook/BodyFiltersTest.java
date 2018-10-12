package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.UnaryOperator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

final class BodyFiltersTest {

    @Test
    void shouldFilterAccessTokenByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("application/json", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"XXX\"}"));
    }

    @Test
    void shouldNotFilterAccessTokenInTextPlainByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("text/plain", "{\"access_token\":\"secret\"}");

        assertThat(actual, is("{\"access_token\":\"secret\"}"));
    }

    @Test
    void shouldFilterClientSecretByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("application/x-www-form-urlencoded", "client_secret=secret");

        assertThat(actual, is("client_secret=XXX"));
    }

    @Test
    void shouldNotFilterClientSecretInTextPlainByDefault() {
        final BodyFilter unit = BodyFilters.defaultValue();

        final String actual = unit.filter("text/plain", "client_secret=secret");

        assertThat(actual, is("client_secret=secret"));
    }

    @Test
    void shouldFilterNotEmptyJSONProperty() {
        final BodyFilter unit = BodyFilters.replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterEmptyJSONProperty() {
        final BodyFilter unit = BodyFilters.replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":\"\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldNotFilterNullJSONProperty() {
        final BodyFilter unit = BodyFilters.replaceJsonStringProperty(Collections.singleton("foo"), "XXX");

        final String actual = unit.filter("application/json", "{\"foo\":null,\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":null,\"bar\":\"public\"}"));
    }

    @Test
    void shouldTruncateBodyIfTooLong() {
        final BodyFilter unit = BodyFilters.truncate(5);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual, is("{\"foo..."));
    }

    @Test
    void shouldNotTruncateBodyIfTooShort() {
        final BodyFilter unit = BodyFilters.truncate(50);

        final String actual = unit.filter("application/json", "{\"foo\":\"secret\"}");

        assertThat(actual, is("{\"foo\":\"secret\"}"));
    }

    @Test
    void shouldReturnJsonCompactingBodyFilter() {
        final BodyFilter bodyFilter = BodyFilters.compactJson(new ObjectMapper());

        assertThat(bodyFilter, instanceOf(JsonCompactingBodyFilter.class));
    }

    @Test
    void shouldReturnXmlCompactingBodyFilter() {
        final BodyFilter bodyFilter = BodyFilters.compactXml();

        assertThat(bodyFilter, instanceOf(XmlCompactingBodyFilter.class));
    }

    @Test
    void shouldFilterFormUrlEncodedBodyIfValidRequest() {
        final BodyFilter unit = BodyFilters.replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        final UnaryOperator<String> filter = value -> unit.filter("application/x-www-form-urlencoded", value);

        assertThat(filter.apply("q=boots&sort=price&direction=asc"), is("q=XXX&sort=price&direction=asc"));
        assertThat(filter.apply("sort=price&direction=asc&q=boots"), is("sort=price&direction=asc&q=XXX"));
        assertThat(filter.apply("sort=price&q=boots&direction=asc"), is("sort=price&q=XXX&direction=asc"));
        assertThat(filter.apply("sort=price&direction=asc"), is("sort=price&direction=asc"));
        assertThat(filter.apply("q=boots"), is("q=XXX"));
        assertThat(filter.apply(""), is(""));
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContentType() {
        final BodyFilter unit = BodyFilters.replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        assertThat(unit.filter("application/json", "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(unit.filter("application/xml", "<q>boots</q>"), is("<q>boots</q>"));
        assertThat(unit.filter("invalid", "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(unit.filter(null, "{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
    }

    @Test
    void shouldNotFilterFormUrlEncodedBodyIfNotValidContent() {
        final BodyFilter unit = BodyFilters.replaceFormUrlEncodedProperty(Collections.singleton("q"), "XXX");

        final UnaryOperator<String> filter = value -> unit.filter("application/x-www-form-urlencoded", value);

        assertThat(filter.apply("{\"q\":\"boots\"}"), is("{\"q\":\"boots\"}"));
        assertThat(filter.apply("<q>boots</q>"), is("<q>boots</q>"));
    }
}
