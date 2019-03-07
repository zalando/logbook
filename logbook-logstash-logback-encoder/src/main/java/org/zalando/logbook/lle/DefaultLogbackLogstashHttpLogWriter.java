package org.zalando.logbook.lle;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

public final class DefaultLogbackLogstashHttpLogWriter implements LogbackLogstashLogWriter {

    private final Logger log = LoggerFactory.getLogger(Logbook.class);

    @Override
    public boolean isActive() {
        return log.isTraceEnabled();
    }

    @Override
    public void write(Precorrelation precorrelation, Marker request, String requestMessage) throws IOException {
        log.trace(request, requestMessage);
    }

    @Override
    public void write(Correlation correlation, Marker response, String responseMessage) throws IOException {
        log.trace(response, responseMessage);
    }


}
