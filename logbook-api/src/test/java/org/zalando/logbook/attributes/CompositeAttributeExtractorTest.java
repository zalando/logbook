package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class CompositeAttributeExtractorTest {

    @Test
    void compositeAttributeExtractorShouldExtractAttributes() throws Exception {
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

        when(extractor0.extract(request)).thenThrow(new Exception("ext4-req"));
        when(extractor0.extract(request, response)).thenThrow(new Exception("ext4-resp"));
        final List<String> loggedMessages = new ArrayList<>();
        doAnswer(invocation -> {
            final Exception exception = (Exception) invocation.getArguments()[0];
            loggedMessages.add(exception.getMessage());
            return null;
        }).when(extractor0).logException(any());

        when(extractor1.extract(request)).thenReturn(HttpAttributes.of("ext1-req-key", "ext1-req-val"));
        when(extractor1.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext1-resp-val"));

        when(extractor2.extract(request)).thenReturn(HttpAttributes.of("ext2-req-key", "ext2-req-val"));
        // This should overwrite the value set by extractor1
        when(extractor2.extract(request, response)).thenReturn(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));

        when(extractor3.extract(request)).thenThrow(new Exception("ext3-req"));
        when(extractor3.extract(request, response)).thenThrow(new Exception("ext3-resp"));

        Map<String, String> expected = new HashMap<>();
        expected.put("ext1-req-key", "ext1-req-val");
        expected.put("ext2-req-key", "ext2-req-val");

        assertThat(composite.extract(request)).isEqualTo(new HttpAttributes(expected));
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0)).isEqualTo("ext4-req");

        loggedMessages.clear();
        assertThat(composite.extract(request, response)).isEqualTo(HttpAttributes.of("ext1-resp-key", "ext2-resp-val"));
        assertThat(loggedMessages).hasSize(1);
        assertThat(loggedMessages.get(0)).isEqualTo("ext4-resp");
    }
}
