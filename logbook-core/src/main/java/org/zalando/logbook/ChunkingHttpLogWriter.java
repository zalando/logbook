package org.zalando.logbook;

import lombok.SneakyThrows;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Integer.signum;
import static java.lang.Math.min;
import static java.util.stream.StreamSupport.stream;

public final class ChunkingHttpLogWriter implements HttpLogWriter {

    private final int size;
    private final HttpLogWriter writer;

    public ChunkingHttpLogWriter(final int size, final HttpLogWriter writer) {
        this.size = size;
        this.writer = writer;
    }

    @Override
    public boolean isActive(final RawHttpRequest request) throws IOException {
        return writer.isActive(request);
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) throws IOException {
        split(precorrelation.getRequest()).forEach(throwing(part ->
            writer.writeRequest(new SimplePrecorrelation<>(precorrelation.getId(), part))));
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        split(correlation.getResponse()).forEach(throwing(part ->
                writer.writeResponse(new SimpleCorrelation<>(correlation.getId(), correlation.getRequest(), part))));
    }

    private static <T> Consumer<T> throwing(final ThrowingConsumer<T> consumer) {
        return consumer;
    }

    private interface ThrowingConsumer<T> extends Consumer<T> {

        void tryAccept(@Nonnull T t) throws Exception;

        @Override
        @SneakyThrows
        default void accept(@Nonnull final T t) {
            tryAccept(t);
        }

    }

    private Stream<String> split(final String string) {
        return stream(new StringSpliterator(string, size), false);
    }

    // visible for testing
    static final class StringSpliterator implements Spliterator<String> {

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

            action.accept(string.substring(position, min(string.length(), position + size)));
            position += size;

            return true;
        }

        @Override
        public Spliterator<String> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return string.length() / size + signum(string.length() % size);
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | NONNULL | ORDERED | SIZED;
        }

    }

}
