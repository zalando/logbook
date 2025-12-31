package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import static org.assertj.core.api.Assertions.assertThat;

class FastCompactingJsonBodyFilterTest {

    private final BodyFilter unit = new FastCompactingJsonBodyFilter();

    /*language=JSON*/
    private final String pretty = "{\n" +
            "  \"root\": {\n" +
            "    \"child\": \"text\"\n" +
            "  }\n" +
            "}";

    /*language=JSON*/
    private final String compacted = "{  \"root\": {    \"child\": \"text\"  }}";

    @Test
    void shouldIgnoreEmptyBody() {
        final String filtered = unit.filter("application/json", "");
        assertThat(filtered).isEqualTo("");
    }

    @Test
    void shouldCompactInvalidContent() {
        final String invalidBody = "{\ninvalid}";
        final String filtered = unit.filter("application/json", invalidBody);
        assertThat(filtered).isEqualTo("{invalid}");
    }

    @Test
    void shouldIgnoreInvalidContentType() {
        final String filtered = unit.filter("text/plain", pretty);
        assertThat(filtered).isEqualTo(pretty);
    }

    @Test
    void shouldTransformValidJsonRequestWithSimpleContentType() {
        final String filtered = unit.filter("application/json", pretty);
        assertThat(filtered).isEqualTo(compacted);
    }

    @Test
    void shouldTransformValidJsonRequestWithCompatibleContentType() {
        final String filtered = unit.filter("application/custom+json", pretty);
        assertThat(filtered).isEqualTo(compacted);
    }

    @Test
    void shouldSkipInvalidJsonLookingLikeAValidOne() {
        final String invalidJson = "{invalid}";
        final String filtered = unit.filter("application/custom+json", invalidJson);
        assertThat(filtered).isEqualTo(invalidJson);
    }

}
