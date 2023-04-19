package org.zalando.logbook.core;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class DefaultHttpLogWriter implements HttpLogWriter {

    private final Logger log = LoggerFactory.getLogger(Logbook.class);

    @Override
    public boolean isActive() {
        return log.isTraceEnabled();
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) {
        log.trace(request);
    }

    @Override
    public void write(final Correlation correlation, final String response) {
        log.trace(response);
    }

}
