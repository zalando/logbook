package org.zalando.logbook.core;

import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Clock;

import static java.time.Instant.MIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@NotThreadSafe
final class StreamHttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        assertThat(unit.isActive()).isTrue();
    }

    @Test
    void shouldLogRequestToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.write(new SimplePrecorrelation("", Clock.systemUTC()), "foo");

        verify(stream).println("foo");
    }

    @Test
    void shouldLogResponseToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.write(new SimpleCorrelation("1", MIN, MIN), "bar");

        verify(stream).println("bar");
    }

    @Test
    void shouldRequestToStdoutByDefault() throws IOException {
        final PrintStream original = System.out;
        final PrintStream stream = mock(PrintStream.class);
        System.setOut(stream);

        try {
            final HttpLogWriter unit = new StreamHttpLogWriter();

            unit.write(new SimplePrecorrelation("", Clock.systemUTC()), "foo");

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

            unit.write(new SimpleCorrelation("1", MIN, MIN), "bar");

            verify(stream).println("bar");
        } finally {
            System.setOut(original);
        }
    }

}
