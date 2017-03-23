package org.zalando.logbook;

import java.util.Spliterator;
import java.util.function.Consumer;

class ChunkingSpliterator implements Spliterator<String> {

    private final String string;
    private final int minChunkSize;
    private final int maxChunkSize;
    private int position;

    ChunkingSpliterator(final String string, final int minChunkSize, final int maxChunkSize) {
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("maxChunkSize is expected to be greater than zero");
        }
        if (minChunkSize <= 0) {
            throw new IllegalArgumentException("minChunkSize is expected to be greater than zero");
        }
        if (minChunkSize > maxChunkSize) {
            throw new IllegalArgumentException("minChunkSize is expected to be less or equal to " + maxChunkSize);
        }

        this.string = string;
        this.minChunkSize = minChunkSize;
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super String> action) {
        if (position >= string.length()) {
            return false;
        }

        final String chunk = nextChunk();

        action.accept(chunk);

        position += chunk.length();
        return true;
    }

    @Override
    public Spliterator<String> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        final int size = minChunkSize + (maxChunkSize - minChunkSize) / 2;
        return string.length() / size + Integer.signum(string.length() % size);
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | NONNULL | ORDERED | (minChunkSize == maxChunkSize ? SIZED : 0);
    }

    private String nextChunk() {
        final int chunkMax = position + maxChunkSize;
        if (chunkMax >= string.length()) {
            return string.substring(position);
        }

        final int chunkMin = position + minChunkSize;
        for (int i = chunkMax; i >= chunkMin; i--) {
            final char ch = string.charAt(i - 1);
            if (isSplitCharacter(ch)) {
                return string.substring(position, i);
            }
        }
        return string.substring(position, chunkMax);
    }

    private static boolean isSplitCharacter(final char ch) {
        return ch == ' ' || ch == ',' || ch == ':';
    }
}
