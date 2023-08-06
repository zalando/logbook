package org.zalando.logbook.attributes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class AttributeExtractorTest {

    @Test
    void testAttributeExtractorDefaultMethods() throws Exception {
        final AttributeExtractor attributeExtractor = mock(AttributeExtractor.class);

        doCallRealMethod().when(attributeExtractor).logException(any());
        when(attributeExtractor.extract(any())).thenCallRealMethod();
        when(attributeExtractor.extract(any(), any())).thenCallRealMethod();

        assertDoesNotThrow(() -> attributeExtractor.logException(mock()));
        assertThat(attributeExtractor.extract(mock())).isEqualTo(HttpAttributes.EMPTY);
        assertThat(attributeExtractor.extract(mock(), mock())).isEqualTo(HttpAttributes.EMPTY);
    }

}
