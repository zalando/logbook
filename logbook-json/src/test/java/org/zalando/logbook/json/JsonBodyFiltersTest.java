package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.json.JsonBodyFilters.*;

class JsonBodyFiltersTest {

    private final String contentType = "application/json";

    @Test
    void shouldNotFilterNonJsonMediaType() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("foo"), "XXX");

        final String actual = unit.filter(
                "text/plain", "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"secret\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterPropertyWithQuotationMark() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("password"), "XXX");

        final String actual = unit.filter(
                contentType,
                "{\"password\":\"abc\\\"!?$123\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"password\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterPropertyWithEscapedBackslash() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("password"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"password\":\"abc\\\\\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"password\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterPropertyWithQuotationMarkAndEscapedBackslash() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("password"), "XXX");

        final String actual = unit.filter(
                contentType,
                "{\"password\":\"abc\\\"!?$123\\\\\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"password\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterNotEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("foo"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"foo\":\"secret\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterEmptyJSONProperty() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("foo"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"foo\":\"\",\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void filtersNestedProperty() {
        final BodyFilter unit = replaceJsonStringProperty(singleton("date"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"id\":\"123\",\"meta\":{\"date\":\"2020-07-27\"}}");

        assertThat(actual).isEqualTo("{\"id\":\"123\",\"meta\":{\"date\":\"XXX\"}}");
    }

    @Test
    void shouldFilterNumberProperty() {
        final BodyFilter unit = replaceJsonNumberProperty(
                singleton("foo"), 0);

        final String actual = unit.filter(
                contentType, "{\"foo\":99.8,\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"foo\":0,\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterNullProperty() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("foo"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"foo\":null,\"bar\":\"public\"}");

        assertThat(actual).isEqualTo("{\"foo\":\"XXX\",\"bar\":\"public\"}");
    }

    @Test
    void shouldFilterMatchingNullPropertyOnly() {
        final BodyFilter unit = replaceJsonStringProperty(
                singleton("foo"), "XXX");

        final String actual = unit.filter(
                contentType, "{\"foo\":null,\"bar\":null}");

        assertThat(actual).isEqualTo("{\"foo\":\"XXX\",\"bar\":null}");
    }

    @Test
    void shouldFilterUsingGivenSetSemantics() {
        final SortedSet<String> properties =
                new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        properties.add("FOO");

        final BodyFilter unit = replaceJsonStringProperty(properties, "XXX");

        final String actual = unit.filter(
                contentType, "{\"foo\":null,\"bar\":null}");

        assertThat(actual).isEqualTo("{\"foo\":\"XXX\",\"bar\":null}");
    }

    @Test
    void shouldFilterPrimitives() {
        final BodyFilter unit = replacePrimitiveJsonProperty(
                asList("foo", "bar", "baz")::contains, "XXX");

        final String actual = unit.filter(
                contentType,
                "{\"foo\":1.0,\"bar\":false,\"baz\":\"secret\"}");

        assertThat(actual)
                .isEqualTo("{\"foo\":\"XXX\",\"bar\":\"XXX\",\"baz\":\"XXX\"}");
    }

    @Test
    void shouldFilterAccessTokens() {
        final BodyFilter unit = new AccessTokenBodyFilter();

        final String actual = unit.filter(contentType,
                "{\"access_token\":\"secret\",\"refresh_token\":\"secret\",\"open_id\":\"secret\",\"id_token\":\"secret\",}");

        assertThat(actual)
                .isEqualTo("{\"access_token\":\"XXX\",\"refresh_token\":\"XXX\",\"open_id\":\"XXX\",\"id_token\":\"XXX\",}");
    }

    @Test
    void shouldNotFailToProcessHugeJsonPayload() throws IOException {
        final byte[] bytes = Files.readAllBytes(
                Paths.get("src/test/resources/huge-sample.json"));
        final String body = new String(bytes, UTF_8);

        final BodyFilter unit = replaceJsonStringProperty(
                new HashSet<>(asList(
                        "name", "gender", "phone", "email", "age", "eyeColor")),
                "XXX");
        final String actual = unit.filter(contentType, body);

        assertThat(actual).contains("\"gender\": \"XXX\"");
    }

    @Test
    void supportsVeryLargeValues() throws IOException {
        final byte[] bytes = Files.readAllBytes(
                Paths.get("src/test/resources/huge-value.json"));
        final String body = new String(bytes, UTF_8);

        final BodyFilter unit = replaceJsonStringProperty(
                singleton("password"), "XXX");
        final String actual = unit.filter(contentType, body);

        assertThat(actual).contains("\"password\": \"XXX\"");
    }

    @Test
    void supportsValuesWithManyEscapedDoubleQuotes() throws IOException {
        final byte[] bytes = Files.readAllBytes(
                Paths.get("src/test/resources/many-quotes.json"));
        final String original = new String(bytes, UTF_8);

        final BodyFilter unit = replaceJsonStringProperty(
                singleton("password"), "XXX");
        final String actual = unit.filter(contentType, original);

        assertThat(actual).isEqualTo(original);
    }

    @Test
    void supportsVeryLargeEmbeddedJsonValues() throws IOException {
        final byte[] bytes = Files.readAllBytes(
                Paths.get("src/test/resources/huge-json-value.json"));
        final String original = new String(bytes, UTF_8);

        final BodyFilter unit = replaceJsonStringProperty(
                singleton("informationCode"), "XXX");
        final String actual = unit.filter(contentType, original);

        assertThat(actual).contains("\"informationCode\": \"XXX\"");
    }

    @Test
    void shouldFilterPrimitivesWithFunction() {
        final BodyFilter unit = replacePrimitiveJsonProperty(
                asList("foo", "bar", "baz")::contains, (s, s2) -> s + "XXX" + s2);

        final String actual = unit.filter(
                contentType,
                "{\"foo\":1.0,\"bar\":false,\"baz\":\"secret\"}");

        assertThat(actual)
                .isEqualTo("{\"foo\":\"fooXXX1.0\",\"bar\":\"barXXXfalse\",\"baz\":\"bazXXX\"secret\"\"}");
    }

}
