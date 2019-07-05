package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonNumberProperty;
import static org.zalando.logbook.json.JsonBodyFilters.replaceJsonStringProperty;

class JsonBodyFiltersTest {

    private final String contentType = "application/json";

    @Test
    void shouldNotFilterNonJsonMediaType() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("foo"), "XXX");

        final String actual = unit.filter("text/plain", "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"secret\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterPropertyWithQuotationMark() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("password"), "XXX");

        final String actual = unit.filter(contentType, "{\"password\":\"abc\\\"!?$123\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"password\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterPropertyWithEscapedBackslash() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("password"), "XXX");

        final String actual = unit.filter(contentType, "{\"password\":\"abc\\\\\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"password\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterPropertyWithQuotationMarkAndEscapedBackslash() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("password"), "XXX");

        final String actual = unit.filter(contentType, "{\"password\":\"abc\\\"!?$123\\\\\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"password\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterNotEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("foo"), "XXX");

        final String actual = unit.filter(contentType, "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("foo"), "XXX");

        final String actual = unit.filter(contentType, "{\"foo\":\"\",\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterNumberProperty() {
        final BodyFilter unit = replaceJsonNumberProperty(singleton("foo"), 0);

        final String actual = unit.filter(contentType, "{\"foo\":99.8,\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":0,\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterNullProperty() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("foo"), "XXX");

        final String actual = unit.filter(contentType, "{\"foo\":null,\"bar\":\"public\"}");

        assertThat(actual, is("{\"foo\":\"XXX\",\"bar\":\"public\"}"));
    }

    @Test
    void shouldFilterAccessTokens() {
        final BodyFilter unit = new AccessTokenBodyFilter();

        final String actual = unit.filter(contentType,
                "{\"access_token\":\"secret\",\"refresh_token\":\"secret\",\"open_id\":\"secret\",\"id_token\":\"secret\",}");

        assertThat(actual, is("{\"access_token\":\"XXX\",\"refresh_token\":\"XXX\",\"open_id\":\"XXX\",\"id_token\":\"XXX\",}"));
    }

    @Test
    void shouldNotFailToProcessHugeJsonPayload() throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/huge-sample.json"));
        final String body = new String(bytes, UTF_8);

        final BodyFilter unit = replaceJsonStringProperty(new HashSet<>(asList("name", "gender", "phone", "email", "age", "eyeColor")), "XXX");
        final String actual = unit.filter(contentType, body);

        assertThat(actual, containsString("\"gender\": \"XXX\""));
    }

}
