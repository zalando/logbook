package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

public final class RawHttpRequestTest {

    @Test
    public void withoutBodyShouldDefaultToNoOp() throws IOException {
        final RawHttpRequest unit = mock(RawHttpRequest.class);
        doCallRealMethod().when(unit).withoutBody();
        unit.withoutBody();
    }

}