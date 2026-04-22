package org.zalando.logbook.core;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;

import java.io.IOException;
@RequiredArgsConstructor
public final class StatusCodeBasedSink implements Sink {

    private static final Logger log = LoggerFactory.getLogger(Logbook.class);

    private final HttpLogFormatter formatter;

    @Override
    public boolean isActive() {
        return log.isEnabledForLevel(Level.TRACE);
    }

    @Override
    public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
        final String message = formatter.format(precorrelation, request);
        log.atLevel(Level.TRACE).log(message);
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request,
            final HttpResponse response) throws IOException {
        final String message = formatter.format(correlation, response);
        final int status = response.getStatus();
        final Level level;

        if (status < 400) {
            level = Level.TRACE;
        } else if (status < 500) {
            level = Level.WARN;
        } else {
            level = Level.ERROR;
        }

        log.atLevel(level).log(message);
    }

}
