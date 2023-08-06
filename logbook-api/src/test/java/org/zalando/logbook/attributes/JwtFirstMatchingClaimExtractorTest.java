package org.zalando.logbook.attributes;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class JwtFirstMatchingClaimExtractorTest {
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final AttributeExtractor defaultJwtClaimExtractor = JwtFirstMatchingClaimExtractor.builder()
            .build();
    private final AttributeExtractor loggingJwtClaimExtractor = JwtFirstMatchingClaimExtractor.builder()
            .isExceptionLogged(true)
            .build();

    private final Logger logger = (Logger) LoggerFactory.getLogger(JwtFirstMatchingClaimExtractor.class);

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final List<ILoggingEvent> logsList = listAppender.list;

    {
        logger.setLevel(TRACE);
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void cleanUp() {
        logsList.clear();
    }

    @Test
    void shouldReturnCorrectMarker() {
        final JwtFirstMatchingClaimExtractor mockExtractor =
                mock(JwtFirstMatchingClaimExtractor.class, CALLS_REAL_METHODS);
        assertThat(mockExtractor.getLogMarker()).isEqualTo(MarkerFactory.getMarker("JwtFirstMatchingClaimExtractor"));
    }

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
    void shouldHaveNoExtractedAttributesForMalformedJwtBearerTokenAndLogNothingByDefault() {
        // Payload is not Base64-URL encoded
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.C.S"));
        assertThatLogsNothingAndReturnsEmpty();

        // Payload is Base64-URL encoded, but is not a valid JSON ('MTIzNDU2' is the encoding of '12345')
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.MTIzNDU2.S"));
        assertThatLogsNothingAndReturnsEmpty();
    }

    @Test
    void shouldHaveNoExtractedAttributesForMalformedJwtBearerTokenAndLogWhenEnabled() {
        // Payload is not Base64-URL encoded
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.C.S"));
        assertThatLogsAndReturnsEmpty("Input byte[] should at least have 2 bytes for base64 bytes");

        // Payload is Base64-URL encoded, but is not a valid JSON ('MTIzNDU2' is the encoding of '12345')
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.MTIzNDU2.S"));
        assertThatLogsAndReturnsEmpty(
                "Cannot deserialize value of type `java.util.HashMap<java.lang.Object,java.lang.Object>` from Integer"
        );
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
    void shouldExtractSubjectAttributeForJwtBearerTokenWithACustomSubjectClaim() {
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
        assertThatSubjectIs(defaultJwtClaimExtractor, "{}");
    }

    @Test
    void shouldHandleWriteValueAsStringThrowingException() throws Exception {
        final ObjectMapper throwingObjectMapper = mock(ObjectMapper.class);
        final AttributeExtractor customExtractor = JwtFirstMatchingClaimExtractor.builder()
                .objectMapper(throwingObjectMapper)
                .isExceptionLogged(true)
                .build();

        //  'eyJzdWIiOiB7fX0' is the encoding of '{"sub": {}}'
        when(httpRequest.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer H.eyJzdWIiOiB7fX0.S"));

        final HashMap<String, Object> map = new HashMap<>();
        map.put("sub", new Object());
        when(throwingObjectMapper.readValue(any(String.class), eq(HashMap.class))).thenReturn(map);

        final JsonProcessingException exception = mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn("Just a mock exception");

        when(throwingObjectMapper.writeValueAsString(any())).thenThrow(exception);

        assertThatLogsAndReturnsEmpty(customExtractor, "Just a mock exception");
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

    private void assertThatLogsAndReturnsEmpty(final String message) {
        assertThatLogsAndReturnsEmpty(loggingJwtClaimExtractor, message);
    }

    private void assertThatLogsAndReturnsEmpty(AttributeExtractor extractor, final String message) {
        logsList.clear();

        assertThatAttributeIsEmpty(extractor);

        assertThat(logsList).hasSize(1);
        final ILoggingEvent logEvent = logsList.get(0);

        final List<Marker> markerList = logEvent.getMarkerList();
        assertThat(markerList).hasSize(1);
        assertThat(markerList.get(0)).isEqualTo(MarkerFactory.getMarker("JwtFirstMatchingClaimExtractor"));

        final Object[] argumentArray = logEvent.getArgumentArray();
        assertThat(argumentArray).hasSize(1);
        assertThat((String) argumentArray[0]).contains(message);
    }

    private void assertThatLogsNothingAndReturnsEmpty() {
        logsList.clear();

        assertThatAttributeIsEmpty(defaultJwtClaimExtractor);
        assertThat(logsList).hasSize(0);
    }

    @SneakyThrows
    private void assertThatSubjectIs(final AttributeExtractor extractor, final String subject) {
        assertThat(extractor.extract(httpRequest))
                .isEqualTo(HttpAttributes.of("subject", subject));
    }
}
