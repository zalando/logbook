package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class CachingHttpRequestTest {

    @Test
    void shouldDelegate() {
        final CachingHttpRequest unit = new CachingHttpRequest(MockHttpRequest.create());
        assertThat(unit.getHeaders(), is(anEmptyMap()));
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
