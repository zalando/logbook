package org.zalando.logbook;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
public abstract class AbstractHttpStatusCodeSink implements Sink {

    protected final Logger logger;

    protected AbstractHttpStatusCodeSink() {
        this(LoggerFactory.getLogger(Logbook.class));
    }

    protected AbstractHttpStatusCodeSink(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isActive() {
        return logger.isTraceEnabled() || logger.isWarnEnabled() || logger.isErrorEnabled();
    }

    protected LoggingEventBuilder getLoggingEventBuilder(int status) {
        if (status < 400) {
            return logger.atTrace();
        } else if (status < 500) {
            return logger.atWarn();
        } else {
            return logger.atError();
        }
    }
}