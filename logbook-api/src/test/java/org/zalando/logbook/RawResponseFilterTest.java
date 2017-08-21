package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

public final class RawResponseFilterTest {

    @Test
    void noneShouldDefaultToNoOp() {
        final RawResponseFilter unit = RawResponseFilter.none();
        final RawHttpResponse response = mock(RawHttpResponse.class);

        assertThat(unit.filter(response), is(sameInstance(response)));
    }

}
