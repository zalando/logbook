package org.zalando.logbook.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.TRACE;
import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DefaultHttpLogWriterTest {

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
    void shouldUseTraceLevelForActivation() {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setLevel(TRACE);
        assertTrue(unit.isActive());

        logger.setLevel(INFO);
        assertFalse(unit.isActive());
    }

    @Test
    void shouldUseTraceLevelForLoggingRequests() throws IOException {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setLevel(TRACE);

        unit.write(new SimplePrecorrelation("", Clock.systemUTC()), "foo");

        assertEquals("foo", logsList.get(0).getMessage());
        assertEquals(TRACE, logsList.get(0).getLevel());
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setLevel(TRACE);

        unit.write(new SimpleCorrelation("1", MIN, MIN), "bar");

        assertEquals("bar", logsList.get(0).getMessage());
        assertEquals(TRACE, logsList.get(0).getLevel());
    }

}
