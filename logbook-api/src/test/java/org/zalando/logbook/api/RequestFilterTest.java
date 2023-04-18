package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

final class RequestFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final RequestFilter unit = RequestFilter.none();
        final HttpRequest request = mock(HttpRequest.class);

        assertThat(unit.filter(request)).isSameAs(request);
    }

}
