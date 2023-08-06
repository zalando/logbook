package org.zalando.logbook.attributes;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.qos.logback.classic.Level.TRACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

final class JwtBaseExtractorTest {

    private final JwtBaseExtractor extractor =
            createMockExtractor(new ObjectMapper(), Collections.singletonList("sub"), false);

    @Test
    void shouldCorrectlyConvertToString() throws JsonProcessingException {
        final Object anyString = "any string";
        assertThat(extractor.toStringValue(anyString)).isSameAs(anyString);

        final List<Object> anyObject = new ArrayList<>();
        anyObject.add(1);
        anyObject.add("simple string");
        anyObject.add(null);
        assertThat(extractor.toStringValue(anyObject)).isEqualTo("[1,\"simple string\",null]");

        final ObjectMapper throwingMapper = mock(ObjectMapper.class);
        final JsonProcessingException exception = mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn("Just a mock exception");

        when(throwingMapper.writeValueAsString(any())).thenThrow(exception);
        final JwtBaseExtractor extractor2 = createMockExtractor(throwingMapper, Collections.emptyList(), true);
        assertThatThrownBy(() -> extractor2.toStringValue(anyObject))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    void shouldExtractEmptyClaimsWhenRequestHeadersIsNull() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(null);
        assertThat(extractor.extractClaims(request)).isEqualTo(Collections.emptyMap());
    }

    @Test
    void shouldExtractEmptyClaimsWhenClaimNamesListIsEmpty() throws Exception {
        final JwtBaseExtractor extractor = mock(
                JwtBaseExtractor.class,
                withSettings()
                        .useConstructor(new ObjectMapper(), Collections.emptyList(), true)
                        .defaultAnswer(CALLS_REAL_METHODS)
        );
        final HttpRequest request = mock(HttpRequest.class);
        assertThat(extractor.extractClaims(request)).isEqualTo(Collections.emptyMap());
    }

    @Test
    void shouldExtractEmptyClaimsWhenNoAuthHeader() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(HttpHeaders.of("a", "b"));
        assertThat(extractor.extractClaims(request)).isEqualTo(Collections.emptyMap());
    }

    @Test
    void shouldExtractEmptyClaimsWhenAuthHeaderDoesNotMatchPattern() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(HttpHeaders.of("Authorization", "Bearer XYZ"));
        assertThat(extractor.extractClaims(request)).isEqualTo(Collections.emptyMap());
    }

    @Test
    void shouldExtractAllClaimsWhenValidBearerTokenExists() throws Exception {
        final HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(HttpHeaders.of(
                "Authorization",
                "Bearer Header.eyJzdWIiOiAibWUiLCAiaXNzIjogInlvdSIsICJjdXN0b20iOiBbMSwyLDNdfQ.Signature"
        ));

        final Map<String, Object> expected = new HashMap<>();
        expected.put("sub", "me");
        expected.put("iss", "you");
        expected.put("custom", Arrays.asList(1, 2, 3));

        assertThat(extractor.extractClaims(request)).isEqualTo(expected);
    }

    @Test
    void shouldLogDependingOnSettings() {
        final Logger logger = (Logger) LoggerFactory.getLogger(JwtBaseExtractor.class);
        final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        final List<ILoggingEvent> logsList = listAppender.list;

        logger.setLevel(TRACE);
        listAppender.start();
        logger.addAppender(listAppender);

        extractor.logException(new RuntimeException("Just an exception"));
        assertThat(logsList).hasSize(0);

        final JwtBaseExtractor loggingExtractor =
                createMockExtractor(new ObjectMapper(), Collections.singletonList("sub"), true);

        final Marker myMarker = MarkerFactory.getMarker("MyMarker");
        when(loggingExtractor.getLogMarker()).thenReturn(myMarker);

        loggingExtractor.logException(new RuntimeException("Just an exception"));
        assertThat(logsList).hasSize(1);

        final ILoggingEvent logEvent = logsList.get(0);

        final List<Marker> markerList = logEvent.getMarkerList();
        assertThat(markerList).hasSize(1);
        assertThat(markerList.get(0)).isEqualTo(myMarker);

        final Object[] argumentArray = logEvent.getArgumentArray();
        assertThat(argumentArray).hasSize(1);
        assertThat((String) argumentArray[0]).isEqualTo("Just an exception");
    }

    private static JwtBaseExtractor createMockExtractor(ObjectMapper objectMapper,
                                                        List<String> claimNames,
                                                        boolean shouldLogExceptions) {
        return mock(
                JwtBaseExtractor.class,
                withSettings()
                        .useConstructor(objectMapper, claimNames, shouldLogExceptions)
                        .defaultAnswer(CALLS_REAL_METHODS)
        );
    }

}
