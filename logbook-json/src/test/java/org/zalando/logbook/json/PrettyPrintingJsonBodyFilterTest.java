package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PrettyPrintingJsonBodyFilterTest {

    private final BodyFilter unit = new PrettyPrintingJsonBodyFilter();

    /*language=JSON*/
    private final String pretty = "{\n" +
            "  \"root\" : {\n" +
            "    \"child\" : \"text\"\n" +
            "  }\n" +
            "}";

    /*language=JSON*/
    private final String compacted = "{\"root\":{\"child\":\"text\"}}";

    @Test
    void shouldIgnoreEmptyBody() {
        assertThat(unit.filter("application/json", ""), is(""));
    }

    @Test
    void shouldIgnoreInvalidContent() {
        final String invalidBody = "{\ninvalid}";
        final String filtered = unit.filter("application/json", invalidBody);
        assertThat(filtered, is(invalidBody));
    }

    @Test
    void shouldIgnoreInvalidContentType() {
        final String filtered = unit.filter("text/plain", compacted);
        assertThat(filtered, is(compacted));
    }

    @Test
    void shouldTransformValidJsonRequestWithSimpleContentType() {
        final String filtered = unit.filter("application/json", compacted);
        assertThat(filtered, is(pretty));
    }

    @Test
    void shouldTransformValidJsonRequestWithCompatibleContentType() {
        final String filtered = unit.filter("application/custom+json", compacted);
        assertThat(filtered, is(pretty));
    }

    @Test
    void shouldSkipInvalidJsonLookingLikeAValidOne() {
        final String invalidJson = "{invalid}";
        final String filtered = unit.filter("application/custom+json", invalidJson);
        assertThat(filtered, is(invalidJson));
    }

    @Test
    void shouldConstructFromObjectMapper() {
        BodyFilter bodyFilter = new PrettyPrintingJsonBodyFilter(new ObjectMapper());
        final String filtered = bodyFilter.filter("application/json", compacted);
        assertThat(filtered, is(pretty));

    }

}
