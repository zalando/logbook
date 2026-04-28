package org.zalando.logbook.core;

import lombok.RequiredArgsConstructor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

import java.io.IOException;

@RequiredArgsConstructor
public final class StatusCodeBasedSink implements Sink {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter traceWriter;
    private final HttpLogWriter warnWriter;
    private final HttpLogWriter errorWriter;

    @Override
    public boolean isActive() {
        return traceWriter.isActive() || warnWriter.isActive() || errorWriter.isActive();
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final String message = formatter.format(precorrelation, request);
        traceWriter.write(precorrelation, message);
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {
        final String message = formatter.format(correlation, response);
        final int status = response.getStatus();

        if (status < 400) {
            traceWriter.write(correlation, message);
        } else if (status < 500) {
            warnWriter.write(correlation, message);
        } else {
            errorWriter.write(correlation, message);
        }
    }

}
