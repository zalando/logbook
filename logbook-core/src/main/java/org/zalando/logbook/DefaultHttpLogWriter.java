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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class DefaultHttpLogWriter implements HttpLogWriter {

    public enum Level implements Predicate<Logger>, BiConsumer<Logger, String> {

        TRACE(Logger::isTraceEnabled, Logger::trace),
        DEBUG(Logger::isDebugEnabled, Logger::debug),
        INFO(Logger::isInfoEnabled, Logger::info),
        WARN(Logger::isWarnEnabled, Logger::warn),
        ERROR(Logger::isErrorEnabled, Logger::error);

        private final Predicate<Logger> activator;
        private final BiConsumer<Logger, String> consumer;

        Level(final Predicate<Logger> activator, final BiConsumer<Logger, String> consumer) {
            this.activator = activator;
            this.consumer = consumer;
        }

        @Override
        public boolean test(final Logger logger) {
            return activator.test(logger);
        }

        @Override
        public void accept(final Logger logger, final String message) {
            consumer.accept(logger, message);
        }

    }

    private final Logger logger;
    private final Level level;

    public DefaultHttpLogWriter() {
        this(LoggerFactory.getLogger(Logbook.class));
    }

    public DefaultHttpLogWriter(final Logger logger) {
        this(logger, Level.TRACE);
    }

    public DefaultHttpLogWriter(final Logger logger, final Level level) {
        this.logger = logger;
        this.level = level;
    }

    @VisibleForTesting
    Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isActive(final RawHttpRequest request) {
        return level.test(logger);
    }

    @Override
    public void writeRequest(final Precorrelation<String> precorrelation) {
        level.accept(logger, precorrelation.getRequest());
    }

    @Override
    public void writeResponse(final Correlation<String, String> correlation) {
        level.accept(logger, correlation.getResponse());
    }

}
