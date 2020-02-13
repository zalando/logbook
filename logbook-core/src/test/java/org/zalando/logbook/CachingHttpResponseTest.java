package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class CachingHttpResponseTest {

    @Test
    void shouldDelegate() {
        final CachingHttpResponse unit = new CachingHttpResponse(MockHttpResponse.create());
        assertThat(unit.getHeaders(), is(anEmptyMap()));
    }

    @Test
    void shouldCache() {
        final HttpResponse delegate = mock(HttpResponse.class);
        when(delegate.getHeaders()).thenReturn(HttpHeaders.empty());

        final CachingHttpResponse unit = new CachingHttpResponse(delegate);

        unit.getHeaders();
        unit.getHeaders();

        verify(delegate, atMost(1)).getHeaders();
    }

}
