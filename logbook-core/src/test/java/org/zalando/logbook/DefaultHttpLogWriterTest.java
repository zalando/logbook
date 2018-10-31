package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Clock;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DefaultHttpLogWriterTest {

    @Test
    void shouldDefaultToLogbookLogger() {
        final DefaultHttpLogWriter unit = new DefaultHttpLogWriter();

        assertThat(unit.getLogger(), is(equalTo(LoggerFactory.getLogger(Logbook.class))));
    }

    @Test
    void shouldDefaultToTraceLevelForActivation() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.isActive();

        verify(logger).isTraceEnabled();
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingRequests() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.write(new SimplePrecorrelation(Clock.systemUTC()), "foo");

        verify(logger).trace("foo");
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.write(new SimpleCorrelation("1", ZERO), "bar");

        verify(logger).trace("bar");
    }

}
