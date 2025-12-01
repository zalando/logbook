package org.zalando.logbook.json;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.BodyFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CompactingJsonBodyFilterTest {

    private final BodyFilter unit = new CompactingJsonBodyFilter();

    /*language=JSON*/
    private final String pretty = "{\n" +
            "  \"root\": {\n" +
            "    \"child\": \"text\",\n" +
            "    \"float_child\" : 0.40000000000000002" +
            "  }\n" +
            "}";

    /*language=JSON*/
    private final String compacted = "{\"root\":{\"child\":\"text\",\"float_child\":0.4}}";

    @Test
    void shouldIgnoreEmptyBody() {
        final String filtered = unit.filter("application/json", "");
        assertThat(filtered).isEqualTo("");
    }

    @Test
    void shouldIgnoreInvalidContent() {
        final String invalidBody = "{\ninvalid}";
        final String filtered = unit.filter("application/json", invalidBody);
        assertThat(filtered).isEqualTo(invalidBody);
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
    void shouldPreserveBigFloatOnCopy() {
        final String filtered = new CompactingJsonBodyFilter(new PreciseFloatJsonGeneratorWrapper())
                .filter("application/custom+json", pretty);
        final String compactedWithPreciseFloat = "{\"root\":{\"child\":\"text\",\"float_child\":0.40000000000000002}}";
        assertThat(filtered).isEqualTo(compactedWithPreciseFloat);
    }

    @Test
    void shouldLogFloatAsString() {
        final String filtered = new CompactingJsonBodyFilter(new NumberAsStringJsonGeneratorWrapper())
                .filter("application/custom+json", pretty);
        final String compactedWithFloatAsString = "{\"root\":{\"child\":\"text\",\"float_child\":\"0.40000000000000002\"}}";
        assertThat(filtered).isEqualTo(compactedWithFloatAsString);
    }

    @Test
    void shouldSkipInvalidJsonLookingLikeAValidOne() {
        final String invalidJson = "{invalid}";
        final String filtered = unit.filter("application/custom+json", invalidJson);
        assertThat(filtered).isEqualTo(invalidJson);
    }

    @Test
    void shouldUseJackson3WhenAvailable() {
        // This tests the Jackson 3 detection path
        final BodyFilter filter = new CompactingJsonBodyFilter();
        final String filtered = filter.filter("application/json", pretty);
        assertThat(filtered).isEqualTo(compacted);
    }

    @Test
    void shouldUseJackson3WithWrapper() {
        // This tests the Jackson 3 wrapper path
        final BodyFilter filter = new CompactingJsonBodyFilter(new DefaultJsonGeneratorWrapperJackson3());
        final String filtered = filter.filter("application/json", pretty);
        assertThat(filtered).isEqualTo(compacted);
    }

    @Test
    void shouldHandleRuntimeExceptionFromParser() {
        // This tests the RuntimeException catch path
        final String invalidJson = "{\ninvalid}";
        final BodyFilter filter = new CompactingJsonBodyFilter();
        final String filtered = filter.filter("application/json", invalidJson);
        assertThat(filtered).isEqualTo(invalidJson);
    }

}
