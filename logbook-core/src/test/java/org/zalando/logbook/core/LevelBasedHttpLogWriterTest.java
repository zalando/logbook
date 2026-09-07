package org.zalando.logbook.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.OFF;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;
import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LevelBasedHttpLogWriterTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Logbook.class);

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final List<ILoggingEvent> logsList = listAppender.list;

    {
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @BeforeEach
    void reset() {
        logsList.clear();
    }

    @Test
    void isActiveReturnsTrueWhenLevelIsTraceAndLoggerIsTrace() {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.TRACE);
        logger.setLevel(TRACE);

        assertTrue(unit.isActive());
    }

    @Test
    void isActiveReturnsFalseWhenLevelIsTraceAndLoggerIsOff() {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.TRACE);
        logger.setLevel(OFF);

        assertFalse(unit.isActive());
    }

    @Test
    void isActiveReturnsTrueWhenLevelIsWarnAndLoggerIsWarn() {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.WARN);
        logger.setLevel(WARN);

        assertTrue(unit.isActive());
    }

    @Test
    void isActiveReturnsFalseWhenLevelIsWarnAndLoggerIsError() {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.WARN);
        logger.setLevel(ERROR);

        assertFalse(unit.isActive());
    }

    @Test
    void writeRequestLogsMessageAtConfiguredLevel() throws IOException {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.WARN);
        logger.setLevel(WARN);

        unit.write(new SimplePrecorrelation("", Clock.systemUTC()), "foo");

        assertEquals(1, logsList.size());
        final ILoggingEvent event = logsList.get(0);
        assertEquals("foo", event.getMessage());
        assertEquals(WARN, event.getLevel());
    }

    @Test
    void writeResponseLogsMessageAtConfiguredLevel() throws IOException {
        final HttpLogWriter unit = new LevelBasedHttpLogWriter(Level.WARN);
        logger.setLevel(WARN);

        unit.write(new SimpleCorrelation("1", MIN, MIN), "bar");

        assertEquals(1, logsList.size());
        final ILoggingEvent event = logsList.get(0);
        assertEquals("bar", event.getMessage());
        assertEquals(WARN, event.getLevel());
    }

}
