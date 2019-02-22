package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SinkTest {

    @Test
    void shouldBeActiveByDefault() {
        final Sink unit = mock(Sink.class, InvocationOnMock::callRealMethod);

        assertTrue(unit.isActive());
    }

}
