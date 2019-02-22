package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CorrelationTest {

    private final Correlation unit = mock(Correlation.class, InvocationOnMock::callRealMethod);

    @Test
    void correlateNoOp() {
        assertSame(unit, unit.correlate());
    }

}
