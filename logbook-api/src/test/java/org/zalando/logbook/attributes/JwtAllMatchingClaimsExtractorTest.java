package org.zalando.logbook.attributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

final class JwtAllMatchingClaimsExtractorTest {
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final AttributeExtractor defaultJwtClaimExtractor = JwtAllMatchingClaimsExtractor.builder()
            .build();

    @Test
    void shouldHaveNoExtractedAttributesForEmptyClaimNames() throws Exception {
        final AttributeExtractor emptyClaimNamesExtractor = JwtAllMatchingClaimsExtractor.builder()
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
        assertThatAttributeIsEmpty();

        // Payload is Base64-URL encoded, but is not a valid JSON ('MTIzNDU2' is the encoding of '12345')
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.MTIzNDU2.S"));
        assertThatAttributeIsEmpty();
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
    void shouldExtractSubjectAttributeForJwtBearerTokenWithASubjectClaim() {
        //  'eyJzdWIiOiAiam9obiJ9' is the encoding of '{"sub": "john"}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiAiam9obiJ9.S"));
        assertThatSubjectIs(defaultJwtClaimExtractor, "john");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldFilterOutKeysThatAreNotString() throws JsonProcessingException {
        final JwtAllMatchingClaimsExtractor mockExtractor = mock(
                JwtAllMatchingClaimsExtractor.class,
                withSettings().useConstructor(mock(ObjectMapper.class), Collections.singletonList("sub"))
        );
        final Map claims = new HashMap();
        claims.put(1, "One");
        claims.put("sub", "john");
        when(mockExtractor.extractClaims(any())).thenReturn(claims);
        when(mockExtractor.extract(any())).thenCallRealMethod();
        when(mockExtractor.toStringValue("john")).thenReturn("john");
        assertThat(mockExtractor.extract(mock())).isEqualTo(HttpAttributes.of("sub", "john"));
    }

    @Test
    void shouldExtractAllDesiredClaimsAndNothingMore() {
        //  'eyJzdWIiOiAiam9obiIsICJpc3MiOiAiZG9lIiwgImN1c3RvbSI6IFsxLCAiZG9lIiwgbnVsbF19' is the encoding of:
        //  '{"sub": "john", "iss": "doe", "custom": [1, "doe", null]}'
        when(httpRequest.getHeaders()).thenReturn(
                HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiAiam9obiIsICJpc3MiOiAiZG9lIiwgImN1c3RvbSI6IFsxLCAiZG9lIiwgbnVsbF19.S")
        );
        final AttributeExtractor customExtractor = JwtAllMatchingClaimsExtractor.builder()
                .claimNames(Arrays.asList("custom", "sub"))
                .build();

        final Map<String, String> attribs = new HashMap<>();
        attribs.put("sub", "john");
        attribs.put("custom", "[1,\"doe\",null]");

        assertThatAttributesAre(customExtractor, new HttpAttributes(attribs));
    }

    @Test
    void shouldExtractSubjectEvenIfItIsAnArbitraryObject() {
        //  'eyJzdWIiOiB7fX0' is the encoding of '{"sub": {}}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiB7fX0.S"));
        assertThatSubjectIs(defaultJwtClaimExtractor, "{}");
    }

    @Test
    void shouldHandleWriteValueAsStringThrowingException() throws Exception {
        final ObjectMapper throwingObjectMapper = mock(ObjectMapper.class);
        final AttributeExtractor customExtractor = JwtAllMatchingClaimsExtractor.builder()
                .objectMapper(throwingObjectMapper)
                .build();

        //  'eyJzdWIiOiB7fX0' is the encoding of '{"sub": {}}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiB7fX0.S"));

        final HashMap<String, Object> map = new HashMap<>();
        map.put("sub", new Object());
        when(throwingObjectMapper.readValue(any(String.class), eq(HashMap.class))).thenReturn(map);

        final JsonProcessingException exception = mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn("Just a mock exception");

        when(throwingObjectMapper.writeValueAsString(any())).thenThrow(exception);

        assertThatAttributeIsEmpty(customExtractor);
    }

    @SneakyThrows
    private void assertThatAttributeIsEmpty() {
        assertThatAttributeIsEmpty(defaultJwtClaimExtractor);
    }

    @SneakyThrows
    private void assertThatAttributeIsEmpty(final AttributeExtractor extractor) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.EMPTY);
    }

    @SneakyThrows
    private void assertThatSubjectIs(final AttributeExtractor extractor, final String subject) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.of("sub", subject));
    }

    @SneakyThrows
    private void assertThatAttributesAre(final AttributeExtractor extractor, final HttpAttributes attributes) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(attributes);
    }
}
