package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.attributes.HttpAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class HttpRequestTest {

    @Test
    void httpRequestShouldReturnEmptyAttributesByDefault() {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getAttributes()).thenCallRealMethod();
        assertThat(httpRequest.getAttributes()).isEqualTo(HttpAttributes.EMPTY);
    }

}
