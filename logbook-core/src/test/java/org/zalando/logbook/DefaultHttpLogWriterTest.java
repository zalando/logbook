package org.zalando.logbook;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static java.time.Duration.ZERO;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DefaultHttpLogWriterTest {

    @Test
    public void shouldDefaultToLogbookLogger() {
        final DefaultHttpLogWriter unit = new DefaultHttpLogWriter();

        assertThat(unit.getLogger(), is(equalTo(LoggerFactory.getLogger(Logbook.class))));
    }

    @Test
    public void shouldDefaultToTraceLevelForActivation() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.isActive(mock(RawHttpRequest.class));

        verify(logger).isTraceEnabled();
    }

    @Test
    public void shouldDefaultToTraceLevelForLoggingRequests() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        verify(logger).trace("foo");
    }

    @Test
    public void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar"));

        verify(logger).trace("bar");
    }

}