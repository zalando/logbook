package org.zalando.logbook.attributes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class AllAttributesExtractorTest {

    private HttpAttributes expectedAttributes;

    @BeforeEach
    void setUp() {
        Map<String, Object> attributesMap = new HashMap<>();
        attributesMap.put("foo", "bar");
        attributesMap.put("fizz", "buzz");
        expectedAttributes = new HttpAttributes(attributesMap);
    }

    @Test
    void shouldHaveAllAttributesFromRequest() {
        HttpRequest request = createRequest();

        AllAttributesExtractor extractor = new AllAttributesExtractor();
        assertEquals(expectedAttributes, extractor.extract(request));
    }

    @Test
    void shouldHaveAllAttributesFromRequestAndResponse() {
        HttpRequest request = createRequest();
        HttpResponse response = mock(HttpResponse.class);

        AllAttributesExtractor extractor = new AllAttributesExtractor();
        assertEquals(expectedAttributes, extractor.extract(request, response));
    }

    private HttpRequest createRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getAttributes()).thenReturn(expectedAttributes);
        return request;
    }
}