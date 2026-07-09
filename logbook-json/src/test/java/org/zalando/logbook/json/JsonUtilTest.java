package org.zalando.logbook.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    private final JsonMapper mapper = new JsonMapper();

    static Stream<String> validJsonInputs() {
        return Stream.of(
                "{}",
                "[]",
                "\"test\"",
                "123",
                "true",
                "false",
                "null",
                " \n\t { \n\t } \n\t "
        );
    }

    @ParameterizedTest
    @MethodSource("validJsonInputs")
    void shouldAcceptValidJsonStartAndEnd(final String input) {
        assertThat(JsonUtil.looksLikeJson(input))
                .as("Expected looksLikeJson(\"%s\") to be true", input)
                .isTrue();
    }

    static Stream<String> invalidJsonInputs() {
        return Stream.of(
                "",
                "   ",
                "abc",
                "\"test"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonInputs")
    void shouldRejectInvalidJsonStartAndEnd(final String input) {
        assertThat(JsonUtil.looksLikeJson(input))
                .as("Expected looksLikeJson(\"%s\") to be false", input)
                .isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{foo}"})
    void shouldAcceptSyntacticallyPlausibleButInvalidJson(final String input) {
        // Ends with '}', looksLikeJson returns true, parser will fail
        assertThat(JsonUtil.looksLikeJson(input))
                .as("Expected looksLikeJson(\"%s\") to be true (syntactically plausible)", input)
                .isTrue();
    }

    static Stream<String> validJsonForParsing() {
        return Stream.of(
                "{}",
                "[]",
                "{\"foo\":\"bar\"}"
        );
    }

    @ParameterizedTest
    @MethodSource("validJsonForParsing")
    void shouldValidateValidJson(final String input) {
        assertThat(JsonUtil.isValidJson(input, mapper))
                .as("Expected isValidJson(\"%s\") to be true", input)
                .isTrue();
    }

    static Stream<String> invalidJsonForParsing() {
        return Stream.of(
                "{foo}",
                "{\"foo\":\"bar\"",
                "no healthy upstream"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidJsonForParsing")
    void shouldRejectInvalidJson(final String input) {
        assertThat(JsonUtil.isValidJson(input, mapper))
                .as("Expected isValidJson(\"%s\") to be false", input)
                .isFalse();
    }
}
