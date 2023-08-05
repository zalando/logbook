package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

final class NoOpAttributeExtractorTest {

    @Test
    void shouldHaveNoExtractedAttributesFromRequest() throws Exception {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        final AttributeExtractor attributesExtractor = new NoOpAttributeExtractor();
        assertThat(attributesExtractor.extract(httpRequest)).isEqualTo(HttpAttributes.EMPTY);
    }

    @Test
    void shouldHaveNoExtractedAttributesFromRequestAndResponse() throws Exception {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        final HttpResponse httpResponse = mock(HttpResponse.class);
        final AttributeExtractor attributesExtractor = new NoOpAttributeExtractor();
        assertThat(attributesExtractor.extract(httpRequest, httpResponse)).isEqualTo(HttpAttributes.EMPTY);
    }
}
