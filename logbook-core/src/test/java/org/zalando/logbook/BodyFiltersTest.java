package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public final class BodyFiltersTest {

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
}
