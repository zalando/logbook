package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

final class NoOpRequestAttributesExtractorTest {

    @Test
    void shouldHaveNoExtractedAttributes() {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        final RequestAttributesExtractor attributesExtractor = new NoOpRequestAttributesExtractor();
        assertThat(attributesExtractor.extractOrEmpty(httpRequest)).isEqualTo(HttpAttributes.EMPTY);
    }
}
