package org.zalando.logbook.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class JwtFirstMatchingClaimExtractorTest {
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final AttributeExtractor jwtClaimExtractor = JwtFirstMatchingClaimExtractor.builder().build();

    @Test
    void shouldHaveNoExtractedAttributesForEmptyClaimNames() throws Exception {
        final AttributeExtractor emptyClaimNamesExtractor = JwtFirstMatchingClaimExtractor.builder()
                .claimNames(Collections.emptyList())
                .build();

        assertThat(emptyClaimNamesExtractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.EMPTY);
    }

    @Test
    void shouldHaveNoExtractedAttributesForNullHeaders() {
        when(httpRequest.getHeaders()).thenReturn(null);
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForEmptyHeaders() {
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.empty());
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForHeadersWithoutAuthorization() {
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Content-Type", "application/yaml"));
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForNonBearerAuthorization() {
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "XYZ"));
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForNonJwtBearerToken() {
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer XYZ"));
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForMalformedJwtBearerToken() {
        // Payload is not Base64-URL encoded
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.C.S"));
        assertThatThrowsAndReturnsEmpty(IllegalArgumentException.class, "Input byte[] should at least have 2 bytes for base64 bytes");

        // Payload is Base64-URL encoded, but is not a valid JSON ('MTIzNDU2' is the encoding of '12345')
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.MTIzNDU2.S"));
        assertThatThrowsAndReturnsEmpty(JsonProcessingException.class,
                "Cannot deserialize value of type `java.util.HashMap<java.lang.Object,java.lang.Object>` from Integer");
    }

    @Test
    void shouldHaveNoExtractedAttributesForJwtBearerTokenWithEmptyClaims() {
        //  'e30' is the encoding of '{}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.e30.S"));
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForJwtBearerTokenWithoutSubjectClaim() {
        //  'eyJhIjogMX0' is the encoding of '{"a": 1}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJhIjogMX0.S"));
        assertThatAttributeIsEmpty();
    }

    @Test
    void shouldExtractedSubjectAttributeForJwtBearerTokenWithASubjectClaim() {
        //  'eyJzdWIiOiAiam9obiJ9' is the encoding of '{"sub": "john"}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiAiam9obiJ9.S"));
        assertThatSubjectIs(jwtClaimExtractor, "john");
    }

    @Test
    void shouldExtractedSubjectAttributeForJwtBearerTokenWithACustomSubjectClaim() {
        //  'eyJzdWIiOiAiam9obiIsICJjdXN0b20iOiAiZG9lIn0' is the encoding of '{"sub": "john", "custom": "doe"}'
        when(httpRequest.getHeaders()).thenReturn(
                HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiAiam9obiIsICJjdXN0b20iOiAiZG9lIn0.S")
        );
        final AttributeExtractor customExtractor = JwtFirstMatchingClaimExtractor.builder()
                .claimNames(Arrays.asList("custom", "sub"))
                .build();
        assertThatSubjectIs(customExtractor, "doe");
    }

    @Test
    void shouldExtractSubjectEvenIfItIsAnArbitraryObject() {
        //  'eyJzdWIiOiB7fX0' is the encoding of '{"sub": {}}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiB7fX0.S"));
        assertThatSubjectIs(jwtClaimExtractor, "{}");
    }

    @Test
    void shouldHandleWriteValueAsStringThrowingException() throws Exception {
        ObjectMapper throwingObjectMapper = mock(ObjectMapper.class);
        final AttributeExtractor throwingExtractor = JwtFirstMatchingClaimExtractor.builder()
                .objectMapper(throwingObjectMapper)
                .build();

        //  'eyJzdWIiOiB7fX0' is the encoding of '{"sub": {}}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiB7fX0.S"));

        HashMap<String, Object> map = new HashMap<>();
        map.put("sub", new Object());
        when(throwingObjectMapper.readValue(any(String.class), eq(HashMap.class))).thenReturn(map);

        // Looking at the source code of "writeValueAsString", this is how an exception is thrown
        when(throwingObjectMapper.writeValueAsString(any()))
                .thenThrow(JsonMappingException.fromUnexpectedIOE(new IOException()));

        assertThatThrownBy(() -> throwingExtractor.extract(httpRequest))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JsonMappingException.class);
    }

    @SneakyThrows
    private void assertThatAttributeIsEmpty() {
        assertThat(jwtClaimExtractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.EMPTY);
    }

    private void assertThatThrowsAndReturnsEmpty(final Class<?> exceptionClass, final String message) {
        assertThatThrownBy(() -> jwtClaimExtractor.extract(httpRequest))
                .isInstanceOf(exceptionClass)
                .hasMessageContaining(message);
    }

    @SneakyThrows
    private void assertThatSubjectIs(final AttributeExtractor extractor, final String subject) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.of("subject", subject));
    }
}
