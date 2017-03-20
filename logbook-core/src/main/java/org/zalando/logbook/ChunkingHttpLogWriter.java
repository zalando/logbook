package org.zalando.logbook;

import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

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
        for (final String part : split(precorrelation.getRequest())) {
            writer.writeRequest(new SimplePrecorrelation<>(precorrelation.getId(), part));
        }
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) throws IOException {
        for (final String part : split(correlation.getResponse())) {
            writer.writeResponse(new SimpleCorrelation<>(correlation.getId(), correlation.getRequest(), part));
        }
    }

    private Iterable<String> split(final String s) {
        final List<String> parts = new ArrayList<>(s.length() / size + 1);

        for (int i = 0; i < s.length(); i += size) {
            parts.add(s.substring(i, min(s.length(), i + size)));
        }
        return parts;
    }


}
