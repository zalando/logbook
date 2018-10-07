package org.zalando.logbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JsonCompactingBodyFilterTest {

    private JsonCompactingBodyFilter bodyFilter;

    /*language=JSON*/
    private final String prettifiedJson = "{\n" +
            "  \"root\": {\n" +
            "    \"child\": \"text\"\n" +
            "  }\n" +
            "}";

    /*language=JSON*/
    private final String minimisedJson = "{\"root\":{\"child\":\"text\"}}";

    @BeforeEach
    void setUp() {
        bodyFilter = new JsonCompactingBodyFilter(new ObjectMapper());
    }

    @Test
    void shouldIgnoreEmptyBody() {
        final String filtered = bodyFilter.filter("application/json", "");
        assertThat(filtered, is(""));
    }

    @Test
    void shouldIgnoreInvalidContent() {
        final String invalidBody = UUID.randomUUID().toString();
        final String filtered = bodyFilter.filter("application/json", invalidBody);
        assertThat(filtered, is(invalidBody));
    }

    @Test
    void shouldIgnoreInvalidContentType() {
        final String filtered = bodyFilter.filter("text/plain", prettifiedJson);
        assertThat(filtered, is(prettifiedJson));
    }

    @Test
    void shouldTransformValidJsonRequestWithSimpleContentType() {
        final String filtered = bodyFilter.filter("application/json", prettifiedJson);
        assertThat(filtered, is(minimisedJson));
    }

    @Test
    void shouldTransformValidJsonRequestWithCompatibleContentType() {
        final String filtered = bodyFilter.filter("application/custom+json", prettifiedJson);
        assertThat(filtered, is(minimisedJson));
    }

    @Test
    void shouldSkipInvalidJsonLookingLikeAValidOne() {
        final String invalidJson = "{invalid}";
        final String filtered = bodyFilter.filter("application/custom+json", invalidJson);
        assertThat(filtered, is(invalidJson));
    }
}