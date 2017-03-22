package org.zalando.logbook;

import java.util.Spliterator;
import java.util.function.Consumer;

class StringSpliterator implements Spliterator<String> {

    private final String string;
    private final int size;
    private int position;

    StringSpliterator(final String string, final int size) {
        this.string = string;
        this.size = size;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super String> action) {
        if (position >= string.length()) {
            return false;
        }
        action.accept(string.substring(position, Math.min(string.length(), position + size)));
        position += size;
        return true;
    }

    @Override
    public Spliterator<String> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return string.length() / size + Integer.signum(string.length() % size);
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | NONNULL | ORDERED | SIZED;
    }
}
