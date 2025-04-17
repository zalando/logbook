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

        // Reset next to 0, we've exhausted the tasks and this instance will live on in the thread
        // that it was created in, so we need to reset next back to 0 if we want to see any more output

        next = 0;
    }

    boolean hasSecondTask() {
        return tasks.get(1) != null;
    }
}
