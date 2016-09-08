package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RawResponseFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final RawResponseFilter unit = RawResponseFilter.none();
        final RawHttpResponse response = mock(RawHttpResponse.class);

        assertThat(unit.filter(response), is(sameInstance(response)));
    }

}