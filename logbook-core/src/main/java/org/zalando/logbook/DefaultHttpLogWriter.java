package org.zalando.logbook;

/*
 * #%L
 * Logbook
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class DefaultHttpLogWriter implements HttpLogWriter {

    public enum Level {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

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

    //@VisibleForTesting
    Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isActive(final RawHttpRequest request) {
        return activator.test(logger);
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) {
        consumer.accept(logger, precorrelation.getRequest());
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) {
        consumer.accept(logger, correlation.getResponse());
    }

}
