package org.zalando.logbook.spring.webflux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class HttpHeadersConverterTest {

    @Test
    void shouldConvertToMultiMap() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Header1", "Value1");
        httpHeaders.add("Header2", "Value1");
        httpHeaders.add("Header2", "Value2");
        HttpHeadersConverter compatibility = HttpHeadersConverter.of(httpHeaders);

        Map<String, List<String>> actual = compatibility.toMultiValueMap();

        Assertions.assertThat(actual)
                .hasSize(2)
                .containsOnlyKeys("Header1", "Header2")
                .containsEntry("Header1", Arrays.asList("Value1"))
                .containsEntry("Header2", Arrays.asList("Value1", "Value2"));
    }

    @Test
    void shouldConvertToEmptyMultiMapWhenEmptyHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpHeadersConverter compatibility = HttpHeadersConverter.of(httpHeaders);

        Map<String, List<String>> actual = compatibility.toMultiValueMap();

        Assertions.assertThat(actual.isEmpty()).isTrue();
    }
}