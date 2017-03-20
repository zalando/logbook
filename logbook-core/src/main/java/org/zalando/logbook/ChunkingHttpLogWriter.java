package org.zalando.logbook;

import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.regex.Pattern;

public final class ChunkingHttpLogWriter implements HttpLogWriter {

    private final Pattern pattern;
    private final HttpLogWriter writer;

    public ChunkingHttpLogWriter(final int size, final HttpLogWriter writer) {
        this.pattern = Pattern.compile("(?<=\\G.{" + size + "})");
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

    private String[] split(final String s) {
        return pattern.split(s);
    }

}
