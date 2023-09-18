package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.attributes.HttpAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

final class HttpRequestTest {

    @Test
    void httpRequestShouldReturnEmptyAttributesByDefault() {
        final HttpRequest httpRequest = mock(HttpRequest.class, CALLS_REAL_METHODS);
        assertThat(httpRequest.getAttributes()).isEqualTo(HttpAttributes.EMPTY);
    }

}
