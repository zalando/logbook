package org.zalando.logbook;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.IOException;
import java.time.Clock;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.org.lidalia.slf4jext.Level.INFO;
import static uk.org.lidalia.slf4jext.Level.TRACE;

final class DefaultHttpLogWriterTest {

    private final TestLogger logger = TestLoggerFactory.getTestLogger(Logbook.class);

    @BeforeEach
    @AfterEach
    void reset() {
        TestLoggerFactory.clear();
    }

    @Test
    void shouldUseTraceLevelForActivation() {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setEnabledLevels(TRACE);
        assertTrue(unit.isActive());

        logger.setEnabledLevels(INFO);
        assertFalse(unit.isActive());
    }

    @Test
    void shouldUseTraceLevelForLoggingRequests() throws IOException {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setEnabledLevels(TRACE);

        unit.write(new SimplePrecorrelation(() -> "", Clock.systemUTC()), "foo");

        final LoggingEvent event = getOnlyElement(logger.getLoggingEvents());

        assertEquals(TRACE, event.getLevel());
        assertEquals("foo", event.getMessage());
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final HttpLogWriter unit = new DefaultHttpLogWriter();

        logger.setEnabledLevels(TRACE);

        unit.write(new SimpleCorrelation("1", MIN, MIN), "bar");

        final LoggingEvent event = getOnlyElement(logger.getLoggingEvents());

        assertEquals(TRACE, event.getLevel());
        assertEquals("bar", event.getMessage());
    }

}
