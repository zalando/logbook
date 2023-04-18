package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.HttpHeaders;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.test.MockHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class CachingHttpRequestTest {

    @Test
    void shouldDelegate() {
        final CachingHttpRequest unit = new CachingHttpRequest(MockHttpRequest.create());
        assertThat(unit.getHeaders()).isEmpty();
    }

    @Test
    void shouldCache() {
        final HttpRequest delegate = mock(HttpRequest.class);
        when(delegate.getHeaders()).thenReturn(HttpHeaders.empty());

        final CachingHttpRequest unit = new CachingHttpRequest(delegate);

        unit.getHeaders();
        unit.getHeaders();

        verify(delegate, atMost(1)).getHeaders();
    }

}
