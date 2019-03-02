package org.zalando.logbook;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.zalando.fauxpas.FauxPas.throwingConsumer;

@API(status = EXPERIMENTAL)
public final class ChunkingSink implements Sink {

    private static final int MIN_MAX_DELTA = 16;

    private final Sink delegate;
    private final int minChunkSize;
    private final int maxChunkSize;

    public ChunkingSink(final Sink delegate, final int size) {
        this.delegate = delegate;

        if (size <= 0) {
            throw new IllegalArgumentException("size is expected to be greater than zero");
        }
        this.minChunkSize = size > MIN_MAX_DELTA ? size - MIN_MAX_DELTA : size;
        this.maxChunkSize = size;
    }

    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest original) throws IOException {
        chunk(original)
                .map(chunk -> new BodyReplacementHttpRequest(original, chunk))
                .forEach(throwingConsumer(replaced -> delegate.write(precorrelation, replaced)));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse original) throws IOException {
        chunk(original)
                .map(chunk -> new BodyReplacementHttpResponse(original, chunk))
                .forEach(throwingConsumer(replaced -> delegate.write(correlation, request, replaced)));
    }

    private Stream<String> chunk(final HttpMessage message) throws IOException {
        return stream(new ChunkingSpliterator(message.getBodyAsString(), minChunkSize, maxChunkSize), false);
    }

}
