package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

public final class RawHttpResponseTest {

    @Test
    void withoutBodyShouldDefaultToNoOp() throws IOException {
        final RawHttpResponse unit = mock(RawHttpResponse.class);
        doCallRealMethod().when(unit).withoutBody();
        unit.withoutBody();
    }

}
