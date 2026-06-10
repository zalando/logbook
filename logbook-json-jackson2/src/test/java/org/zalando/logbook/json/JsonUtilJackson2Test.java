package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldAcceptValidJsonStartAndEnd() {
        assertThat(JsonUtilJackson2.looksLikeJson("{}")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("[]")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("\"test\"")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("123")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("true")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("false")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson("null")).isTrue();
        assertThat(JsonUtilJackson2.looksLikeJson(" \n\t { \n\t } \n\t ")).isTrue();
    }

    @Test
    void shouldRejectInvalidJsonStartAndEnd() {
        assertThat(JsonUtilJackson2.looksLikeJson("")).isFalse();
        assertThat(JsonUtilJackson2.looksLikeJson("   ")).isFalse();
        assertThat(JsonUtilJackson2.looksLikeJson("abc")).isFalse();
        assertThat(JsonUtilJackson2.looksLikeJson("\"test")).isFalse();
        assertThat(JsonUtilJackson2.looksLikeJson("{foo}")).isTrue(); // Ends with '}', looksLikeJson returns true, parser will fail
    }

    @Test
    void shouldValidateJson() {
        assertThat(JsonUtilJackson2.isValidJson("{}", mapper)).isTrue();
        assertThat(JsonUtilJackson2.isValidJson("[]", mapper)).isTrue();
        assertThat(JsonUtilJackson2.isValidJson("{\"foo\":\"bar\"}", mapper)).isTrue();
        
        assertThat(JsonUtilJackson2.isValidJson("{foo}", mapper)).isFalse();
        assertThat(JsonUtilJackson2.isValidJson("{\"foo\":\"bar\"", mapper)).isFalse();
        assertThat(JsonUtilJackson2.isValidJson("no healthy upstream", mapper)).isFalse();
    }
}
