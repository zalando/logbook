package org.zalando.logbook.core.attributes;

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

    @Test
    void compositeAttributeExtractorShouldExtractAttributes() {
        final HttpRequest request = mock(HttpRequest.class);
        final HttpResponse response = mock(HttpResponse.class);
        final AttributeExtractor extractor1 = mock(AttributeExtractor.class);
        final AttributeExtractor extractor2 = mock(AttributeExtractor.class);
        final AttributeExtractor extractor3 = mock(AttributeExtractor.class);
        final AttributeExtractor extractor0 = mock(AttributeExtractor.class);
        final List<AttributeExtractor> extractors = new ArrayList<>();

        extractors.add(extractor1);
        extractors.add(extractor2);
        extractors.add(extractor3);
        extractors.add(extractor0);

        final CompositeAttributeExtractor composite = new CompositeAttributeExtractor(extractors);

        when(extractor0.extract(request)).thenThrow(new RuntimeException("ext4-req"));
        when(extractor0.extract(request, response)).thenThrow(new RuntimeException("ext4-resp"));

        when(extractor1.extract(request)).thenReturn(HttpAttributes.of("ext1-req-key", "ext1-req-val"));
        when(extractor1.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext1-resp-val"));

        when(extractor2.extract(request)).thenReturn(HttpAttributes.of("ext2-req-key", "ext2-req-val"));
        // This should overwrite the value set by extractor1
        when(extractor2.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));

        when(extractor3.extract(request)).thenThrow(new RuntimeException("ext3-req"));
        when(extractor3.extract(request, response)).thenThrow(new RuntimeException("ext3-resp"));

        Map<String, Object> expected = new HashMap<>();
        expected.put("ext1-req-key", "ext1-req-val");
        expected.put("ext2-req-key", "ext2-req-val");

        assertThat(composite.extract(request)).isEqualTo(new HttpAttributes(expected));
        assertThat(composite.extract(request, response)).isEqualTo(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));
    }
}
