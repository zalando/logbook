package org.zalando.logbook.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldAcceptValidJsonStartAndEnd() {
        assertThat(JsonUtil.looksLikeJson("{}")).isTrue();
        assertThat(JsonUtil.looksLikeJson("[]")).isTrue();
        assertThat(JsonUtil.looksLikeJson("\"test\"")).isTrue();
        assertThat(JsonUtil.looksLikeJson("123")).isTrue();
        assertThat(JsonUtil.looksLikeJson("true")).isTrue();
        assertThat(JsonUtil.looksLikeJson("false")).isTrue();
        assertThat(JsonUtil.looksLikeJson("null")).isTrue();
        assertThat(JsonUtil.looksLikeJson(" \n\t { \n\t } \n\t ")).isTrue();
    }

    @Test
    void shouldRejectInvalidJsonStartAndEnd() {
        assertThat(JsonUtil.looksLikeJson("")).isFalse();
        assertThat(JsonUtil.looksLikeJson("   ")).isFalse();
        assertThat(JsonUtil.looksLikeJson("abc")).isFalse();
        assertThat(JsonUtil.looksLikeJson("\"test")).isFalse();
        assertThat(JsonUtil.looksLikeJson("{foo}")).isTrue(); // Ends with '}', looksLikeJson returns true, parser will fail
    }

    @Test
    void shouldValidateJson() {
        assertThat(JsonUtil.isValidJson("{}", mapper)).isTrue();
        assertThat(JsonUtil.isValidJson("[]", mapper)).isTrue();
        assertThat(JsonUtil.isValidJson("{\"foo\":\"bar\"}", mapper)).isTrue();
        
        assertThat(JsonUtil.isValidJson("{foo}", mapper)).isFalse();
        assertThat(JsonUtil.isValidJson("{\"foo\":\"bar\"", mapper)).isFalse();
        assertThat(JsonUtil.isValidJson("no healthy upstream", mapper)).isFalse();
    }
}
