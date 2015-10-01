package org.zalando.logbook;

/*
 * #%L
 * Logbook: Core
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.zalando.logbook.DefaultHttpLogWriter.Level;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public final class DefaultHttpLogWriterLevelTest {

    private final Logger logger = mock(Logger.class);
    private final HttpLogWriter unit;

    private final Predicate<Logger> isEnabled;
    private final BiConsumer<Logger, String> log;

    public DefaultHttpLogWriterLevelTest(final Level level, final Predicate<Logger> isEnabled,
            final BiConsumer<Logger, String> log) {
        this.unit = new DefaultHttpLogWriter(logger, level);
        this.isEnabled = isEnabled;
        this.log = log;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Level.TRACE, activator(Logger::isTraceEnabled), consumer(Logger::trace)},
                {Level.DEBUG, activator(Logger::isDebugEnabled), consumer(Logger::debug)},
                {Level.INFO, activator(Logger::isInfoEnabled), consumer(Logger::info)},
                {Level.WARN, activator(Logger::isWarnEnabled), consumer(Logger::warn)},
                {Level.ERROR, activator(Logger::isErrorEnabled), consumer(Logger::error)},
        });
    }

    private static Predicate<Logger> activator(final Predicate<Logger> predicate) {
        return predicate;
    }

    private static BiConsumer<Logger, String> consumer(final BiConsumer<Logger, String> consumer) {
        return consumer;
    }

    @Test
    public void shouldBeEnabled() throws IOException {
        unit.isActive(mock(RawHttpRequest.class));

        isEnabled.test(verify(logger));
    }

    @Test
    public void shouldLogRequestWithCorrectLevel() throws IOException {
        unit.writeRequest("foo");

        log.accept(verify(logger), "foo");
    }

    @Test
    public void shouldLogResponseWithCorrectLevel() throws IOException {
        unit.writeResponse("bar");

        log.accept(verify(logger), "bar");
    }

}