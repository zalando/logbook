package org.zalando.logbook.netty;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SequenceTest {

    private final Runnable first = mock(Runnable.class, "first");
    private final Runnable second = mock(Runnable.class, "second");
    private final Runnable third = mock(Runnable.class, "third");

    private final Sequence unit = new Sequence(3);

    @Test
    void runFirstEagerly() {
        unit.set(0, first);

        verify(first).run();
    }

    @Test
    void queuesSecond() {
        unit.set(1, second);

        verify(second, never()).run();
    }

    @Test
    void runsRestEagerly() {
        unit.set(2, third);
        unit.set(1, second);
        unit.set(0, first);

        final InOrder inOrder = inOrder(first, second, third);

        inOrder.verify(first).run();
        inOrder.verify(second).run();
        inOrder.verify(third).run();
    }

}
