package org.zalando.logbook.core.attributes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.attributes.AttributeExtractor;
import org.zalando.logbook.attributes.HttpAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class CompositeAttributeExtractorTest {
    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);
    private final AttributeExtractor extractor1 = mock(AttributeExtractor.class);
    private final AttributeExtractor extractor2 = mock(AttributeExtractor.class);
    private final AttributeExtractor extractor3 = mock(AttributeExtractor.class);
    private final AttributeExtractor extractor0 = mock(AttributeExtractor.class);
    private final List<AttributeExtractor> extractors = new ArrayList<>();

    {
        extractors.add(extractor1);
        extractors.add(extractor2);
        extractors.add(extractor3);
        extractors.add(extractor0);
    }

    private final CompositeAttributeExtractor composite = new CompositeAttributeExtractor(extractors);

    @BeforeEach
    void setUp() {
        when(extractor0.extract(request)).thenReturn(HttpAttributes.of("ext0-req-key", "ext0-req-val"));
        when(extractor0.extract(request, response)).thenReturn(HttpAttributes.of("ext0-resp-key", "ext0-resp-val"));

        when(extractor1.extract(request)).thenReturn(HttpAttributes.of("ext1-req-key", "ext1-req-val"));
        when(extractor1.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext1-resp-val"));

        when(extractor2.extract(request)).thenReturn(HttpAttributes.of("ext2-req-key", "ext2-req-val"));
        // This should overwrite the value set by extractor1
        when(extractor2.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));

        when(extractor3.extract(request)).thenReturn(HttpAttributes.of("ext3-req-key", "ext3-req-val"));
        when(extractor3.extract(request, response)).thenReturn(HttpAttributes.of("ext3-resp-key", "ext3-resp-val"));
    }

    @Test
    void compositeAttributeExtractorShouldExtractAttributesWhenNoExceptionsAreThrown() {
        final Map<String, Object> expectedRequestAttributes = new HashMap<>();
        expectedRequestAttributes.put("ext0-req-key", "ext0-req-val");
        expectedRequestAttributes.put("ext1-req-key", "ext1-req-val");
        expectedRequestAttributes.put("ext2-req-key", "ext2-req-val");
        expectedRequestAttributes.put("ext3-req-key", "ext3-req-val");

        assertThat(composite.extract(request)).isEqualTo(new HttpAttributes(expectedRequestAttributes));

        final Map<String, Object> expectedResponseAttributes = new HashMap<>();
        expectedResponseAttributes.put("ext0-resp-key", "ext0-resp-val");
        expectedResponseAttributes.put("ext1-resp-key", "ext2-resp-val");
        expectedResponseAttributes.put("ext3-resp-key", "ext3-resp-val");

        assertThat(composite.extract(request, response)).isEqualTo(new HttpAttributes(expectedResponseAttributes));
    }

    @Test
    void compositeAttributeExtractorShouldExtractAttributesByAllNonThrowingExtractors() {
        when(extractor0.extract(request)).thenThrow(new RuntimeException("ext4-req"));
        when(extractor0.extract(request, response)).thenThrow(new RuntimeException("ext4-resp"));

        when(extractor3.extract(request)).thenThrow(new RuntimeException("ext3-req"));
        when(extractor3.extract(request, response)).thenThrow(new RuntimeException("ext3-resp"));

        final Map<String, Object> expectedRequestAttributes = new HashMap<>();
        expectedRequestAttributes.put("ext1-req-key", "ext1-req-val");
        expectedRequestAttributes.put("ext2-req-key", "ext2-req-val");

        assertThat(composite.extract(request)).isEqualTo(new HttpAttributes(expectedRequestAttributes));
        assertThat(composite.extract(request, response)).isEqualTo(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));
    }
}
