package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

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

        unit.isActive(mock(RawHttpRequest.class));

        verify(logger).isTraceEnabled();
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingRequests() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        verify(logger).trace("foo");
    }

    @Test
    void shouldDefaultToTraceLevelForLoggingResponses() throws IOException {
        final Logger logger = mock(Logger.class);
        final HttpLogWriter unit = new DefaultHttpLogWriter(logger);

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar",
                MockHttpRequest.create(), MockHttpResponse.create()));

        verify(logger).trace("bar");
    }

}
