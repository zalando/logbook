package org.zalando.logbook;

import lombok.AllArgsConstructor;

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

    @Override
    public void writeBoth(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
        write(correlation, request);
        write(correlation, request, response);
    }

}
