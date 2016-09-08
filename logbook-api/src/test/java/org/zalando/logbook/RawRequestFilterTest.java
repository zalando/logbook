package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RawRequestFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final RawRequestFilter unit = RawRequestFilter.none();
        final RawHttpRequest request = mock(RawHttpRequest.class);

        assertThat(unit.filter(request), is(sameInstance(request)));
    }

}