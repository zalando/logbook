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

    private final Runnable fourth = mock(Runnable.class, "fourth");
    private final Runnable fifth = mock(Runnable.class, "fifth");
    private final Runnable sixth = mock(Runnable.class, "sixth");

    private final Runnable seventh = mock(Runnable.class, "seventh");
    private final Runnable eighth = mock(Runnable.class, "eighth");
    private final Runnable ninth = mock(Runnable.class, "ninth");

    private final Runnable tenth = mock(Runnable.class, "tenth");
    private final Runnable eleventh = mock(Runnable.class, "eleventh");

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

    @Test
    void runsMoreEagerlyAllowsSequenceReuse() {
        // reusing the slots of the sequence array should allow reuse
        unit.set(2, ninth);
        unit.set(1, eighth);
        unit.set(0, seventh);

        unit.set(2, sixth);
        unit.set(1, fifth);
        unit.set(0, fourth);

        unit.set(2, third);
        unit.set(1, second);
        unit.set(0, first);

        // still doesn't run partial sequence when reused
        unit.set(2, tenth);
        unit.set(1, eleventh);

        final InOrder inOrder1 = inOrder(seventh, eighth, ninth);
        final InOrder inOrder2 = inOrder(fourth, fifth, sixth);
        final InOrder inOrder3 = inOrder(first, second, third);

        inOrder3.verify(first).run();
        inOrder3.verify(second).run();
        inOrder3.verify(third).run();
        inOrder2.verify(fourth).run();
        inOrder2.verify(fifth).run();
        inOrder2.verify(sixth).run();
        inOrder1.verify(seventh).run();
        inOrder1.verify(eighth).run();
        inOrder1.verify(ninth).run();

        // these two shouldn't run because they are missing their 3rd companion
        verify(tenth, never()).run();
        verify(eleventh, never()).run();
    }

}