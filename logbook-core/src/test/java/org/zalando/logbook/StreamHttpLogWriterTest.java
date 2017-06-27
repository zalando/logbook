package org.zalando.logbook;

import net.jcip.annotations.NotThreadSafe;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.io.PrintStream;

import static java.lang.System.lineSeparator;
import static java.time.Duration.ZERO;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@NotThreadSafe
public final class StreamHttpLogWriterTest {

    @Rule
    public final SystemOutRule stdout = new SystemOutRule().mute().enableLog();

    @Test
    public void shouldBeActiveByDefault() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

    @Test
    public void shouldLogRequestToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        verify(stream).println("foo");
    }

    @Test
    public void shouldLogResponseToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar"));

        verify(stream).println("bar");
    }

    @Test
    public void shouldRequestToStdoutByDefault() throws IOException {
        final HttpLogWriter unit = new StreamHttpLogWriter();

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        assertThat(stdout.getLog(), is("foo" + lineSeparator()));
    }

    @Test
    public void shouldResponseToStdoutByDefault() throws IOException {
        final HttpLogWriter unit = new StreamHttpLogWriter();

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar"));

        assertThat(stdout.getLog(), is("bar" + lineSeparator()));
    }

}