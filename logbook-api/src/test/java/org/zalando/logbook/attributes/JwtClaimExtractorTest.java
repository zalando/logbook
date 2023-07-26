package org.zalando.logbook.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class JwtClaimExtractorTest {
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final RequestAttributesExtractor jwtClaimExtractor = new JwtClaimExtractor();

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
        assertThatThrows(IllegalArgumentException.class, "Input byte[] should at least have 2 bytes for base64 bytes");

        // Payload is Base64-URL encoded, but is not a valid JSON ('MTIzNDU2' is the encoding of '12345')
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.MTIzNDU2.S"));
        assertThatThrows(JsonProcessingException.class,
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
        final RequestAttributesExtractor customExtractor =
                new JwtClaimExtractor(new ObjectMapper(), Arrays.asList("custom", "sub"), "subject");
        assertThatSubjectIs(customExtractor, "doe");
    }

    @SneakyThrows
    private void assertThatAttributeIsEmpty() {
        assertThat(jwtClaimExtractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.EMPTY);
    }

    private void assertThatThrows(final Class<?> exceptionClass, final String message) {
        assertThatThrownBy(() -> jwtClaimExtractor.extract(httpRequest))
                .isInstanceOf(exceptionClass)
                .hasMessageContaining(message);
    }

    @SneakyThrows
    private void assertThatSubjectIs(final RequestAttributesExtractor extractor, final String subject) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.of("subject", subject));
    }
}
