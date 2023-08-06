package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

final class AttributeExtractorTest {

    @Test
    void testAttributeExtractorDefaultMethods() throws Exception {
        final AttributeExtractor attributeExtractor = mock(
                AttributeExtractor.class,
                CALLS_REAL_METHODS
        );

        assertDoesNotThrow(() -> attributeExtractor.logException(mock()));
        assertThat(attributeExtractor.extract(mock())).isEqualTo(HttpAttributes.EMPTY);
        assertThat(attributeExtractor.extract(mock(), mock())).isEqualTo(HttpAttributes.EMPTY);
    }

}
