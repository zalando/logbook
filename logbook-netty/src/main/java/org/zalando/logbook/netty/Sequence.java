package org.zalando.logbook.netty;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.nCopies;

final class Sequence {

    private final List<Runnable> tasks;
    private int next;

    public Sequence(final int length) {
        this.tasks = new LinkedList<>(nCopies(length, null));
    }

    synchronized void set(final int index, final Runnable task) {
        tasks.set(index, task);

        if (index == next) {
            runEagerly();
        }
    }

    private void runEagerly() {
        final int end = tasks.size();

        for (@Nullable final Runnable task : tasks.subList(next, end)) {
            if (task == null) {
                return;
            }

            task.run();
            tasks.set(next, null);
            next++;
        }
    }

}
