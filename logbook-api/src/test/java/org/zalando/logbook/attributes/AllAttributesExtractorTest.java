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

    private HttpAttributes expectedRequestAttributes;
    private HttpAttributes expectedResponseAttributes;

    @BeforeEach
    void setUp() {
        Map<String, Object> requestAttributesMap = new HashMap<>();
        requestAttributesMap.put("foo", "bar");
        requestAttributesMap.put("fizz", "buzz");
        expectedRequestAttributes = new HttpAttributes(requestAttributesMap);

        Map<String, Object> responseAttributesMap = new HashMap<>();
        responseAttributesMap.put("bib", "bob");
        expectedResponseAttributes = new HttpAttributes(responseAttributesMap);
    }

    @Test
    void shouldHaveAllAttributesFromRequest() {
        HttpRequest request = createRequest();

        AllAttributesExtractor extractor = new AllAttributesExtractor();
        assertEquals(expectedRequestAttributes, extractor.extract(request));
    }

    @Test
    void shouldHaveAllAttributesFromRequestAndResponse() {
        HttpRequest request = createRequest();
        HttpResponse response = createResponse();
        HashMap<Object, Object> allAttributes = new HashMap<>();
        allAttributes.putAll(expectedRequestAttributes);
        allAttributes.putAll(expectedResponseAttributes);

        AllAttributesExtractor extractor = new AllAttributesExtractor();
        assertEquals(allAttributes, extractor.extract(request, response));
    }

    private HttpRequest createRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getAttributes()).thenReturn(expectedRequestAttributes);
        return request;
    }

    private HttpResponse createResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getAttributes()).thenReturn(expectedResponseAttributes);
        return response;
    }
}