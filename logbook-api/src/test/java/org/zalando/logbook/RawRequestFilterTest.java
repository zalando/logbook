package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

public final class RawRequestFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final RawRequestFilter unit = RawRequestFilter.none();
        final RawHttpRequest request = mock(RawHttpRequest.class);

        assertThat(unit.filter(request), is(sameInstance(request)));
    }

}
