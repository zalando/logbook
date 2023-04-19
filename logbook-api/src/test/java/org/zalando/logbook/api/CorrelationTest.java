package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CorrelationTest {

    private final Correlation unit = mock(Correlation.class, InvocationOnMock::callRealMethod);

    @Test
    void correlateNoOp() {
        assertThat(unit).isSameAs(unit.correlate());
    }

}
