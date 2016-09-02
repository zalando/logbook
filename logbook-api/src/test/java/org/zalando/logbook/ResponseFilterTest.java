package org.zalando.logbook;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ResponseFilterTest {

    @Test
    public void noneShouldDefaultToNoOp() {
        final ResponseFilter unit = ResponseFilter.none();
        final HttpResponse response = mock(HttpResponse.class);

        assertThat(unit.filter(response), is(sameInstance(response)));
    }

}