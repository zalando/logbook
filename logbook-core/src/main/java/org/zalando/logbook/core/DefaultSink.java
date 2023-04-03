package org.zalando.logbook.core;

import lombok.AllArgsConstructor;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogFormatter;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.api.Sink;

import java.io.IOException;

@AllArgsConstructor
public final class DefaultSink implements Sink {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    @Override
    public boolean isActive() {
        return writer.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        writer.write(precorrelation, formatter.format(precorrelation, request));
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response)
            throws IOException {
        writer.write(correlation, formatter.format(correlation, response));
    }

}
