package org.zalando.logbook;

import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@NotThreadSafe
public final class StreamHttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        assertThat(unit.isActive(), is(true));
    }

    @Test
    void shouldLogRequestToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.write(new SimplePrecorrelation(Clock.systemUTC()), "foo");

        verify(stream).println("foo");
    }

    @Test
    void shouldLogResponseToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.write(new SimpleCorrelation("1", ZERO), "bar");

        verify(stream).println("bar");
    }

    @Test
    void shouldRequestToStdoutByDefault() throws IOException {
        final PrintStream original = System.out;
        final PrintStream stream = mock(PrintStream.class);
        System.setOut(stream);

        try {
            final HttpLogWriter unit = new StreamHttpLogWriter();

            unit.write(new SimplePrecorrelation(Clock.systemUTC()), "foo");

            verify(stream).println("foo");
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void shouldResponseToStdoutByDefault() throws IOException {
        final PrintStream original = System.out;
        final PrintStream stream = mock(PrintStream.class);
        System.setOut(stream);

        try {
            final HttpLogWriter unit = new StreamHttpLogWriter();

            unit.write(new SimpleCorrelation("1", ZERO), "bar");

            verify(stream).println("bar");
        } finally {
            System.setOut(original);
        }
    }

}
