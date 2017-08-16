package org.zalando.logbook;

import lombok.SneakyThrows;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

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
                writer.writeResponse(new SimpleCorrelation<>(
                        correlation.getId(),
                        correlation.getDuration(),
                        correlation.getRequest(),
                        part,
                        correlation.getOriginalRequest(),
                        correlation.getOriginalResponse()))));
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
