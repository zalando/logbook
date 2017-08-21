package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

public final class RequestFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final RequestFilter unit = RequestFilter.none();
        final HttpRequest request = mock(HttpRequest.class);

        assertThat(unit.filter(request), is(sameInstance(request)));
    }

}
