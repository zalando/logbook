package org.zalando.logbook;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
public final class DefaultHttpLogWriter implements HttpLogWriter {

    private final Logger logger;
    private final Predicate<Logger> activator;
    private final BiConsumer<Logger, String> consumer;

    public DefaultHttpLogWriter() {
        this(LoggerFactory.getLogger(Logbook.class));
    }

    public DefaultHttpLogWriter(final Logger logger) {
        this(logger, Level.TRACE);
    }

    public DefaultHttpLogWriter(final Logger logger, final Level level) {
        this.logger = logger;
        this.activator = chooseActivator(level);
        this.consumer = chooseConsumer(level);
    }

    private static Predicate<Logger> chooseActivator(final Level level) {
        switch (level) {
            case DEBUG:
                return Logger::isDebugEnabled;
            case INFO:
                return Logger::isInfoEnabled;
            case WARN:
                return Logger::isWarnEnabled;
            case ERROR:
                return Logger::isErrorEnabled;
            default:
                return Logger::isTraceEnabled;
        }
    }

    private static BiConsumer<Logger, String> chooseConsumer(final Level level) {
        switch (level) {
            case DEBUG:
                return Logger::debug;
            case INFO:
                return Logger::info;
            case WARN:
                return Logger::warn;
            case ERROR:
                return Logger::error;
            default:
                return Logger::trace;
        }
    }

    // visible for testing
    Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isActive() {
        return activator.test(logger);
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) {
        consumer.accept(logger, request);
    }

    @Override
    public void write(final Correlation correlation, final String response) {
        consumer.accept(logger, response);
    }

}
