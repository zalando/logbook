package org.zalando.logbook;

import lombok.SneakyThrows;
import org.apiguardian.api.API;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public final class ChunkingHttpLogWriter implements HttpLogWriter {

    private static final int MIN_MAX_DELTA = 16;

    private final int minChunkSize;
    private final int maxChunkSize;
    private final HttpLogWriter writer;

    public ChunkingHttpLogWriter(final int size, final HttpLogWriter writer) {
        if (size <= 0) {
            throw new IllegalArgumentException("size is expected to be greater than zero");
        }
        this.minChunkSize = size > MIN_MAX_DELTA ? size - MIN_MAX_DELTA : size;
        this.maxChunkSize = size;
        this.writer = writer;
    }

    @Override
    public boolean isActive() {
        return writer.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) {
        split(request).forEach(throwing(part ->
                writer.write(precorrelation, part)));
    }

    @Override
    public void write(final Correlation correlation, final String response) {
        split(response).forEach(throwing(part ->
                writer.write(correlation, part)));
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
        return stream(new ChunkingSpliterator(string, minChunkSize, maxChunkSize), false);
    }
}
