package org.zalando.logbook;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.time.Duration.ZERO;
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
        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        log.accept(verify(logger), "foo");
    }

    @Test
    public void shouldLogResponseWithCorrectLevel() throws IOException {
        unit.writeResponse(new SimpleCorrelation<>("1", ZERO, "foo", "bar"));

        log.accept(verify(logger), "bar");
    }

}