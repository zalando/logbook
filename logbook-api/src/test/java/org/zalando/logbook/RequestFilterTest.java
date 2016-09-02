package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RequestFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final RequestFilter unit = RequestFilter.none();
        final HttpRequest request = mock(HttpRequest.class);

        assertThat(unit.filter(request), is(sameInstance(request)));
    }

}