package org.zalando.logbook;

import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.io.PrintStream;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@NotThreadSafe
public final class StreamHttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

    @Test
    void shouldLogRequestToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

        verify(stream).println("foo");
    }

    @Test
    void shouldLogResponseToStream() throws IOException {
        final PrintStream stream = mock(PrintStream.class);
        final HttpLogWriter unit = new StreamHttpLogWriter(stream);

        unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar", MockHttpRequest.create(),
                MockHttpResponse.create()));

        verify(stream).println("bar");
    }

    @Test
    void shouldRequestToStdoutByDefault() throws IOException {
        final PrintStream original = System.out;
        final PrintStream stream = mock(PrintStream.class);
        System.setOut(stream);

        try {
            final HttpLogWriter unit = new StreamHttpLogWriter();

            unit.writeRequest(new SimplePrecorrelation<>("1", "foo"));

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

            unit.writeResponse(new DefaultLogbook.SimpleCorrelation<>("1", ZERO, "foo", "bar", MockHttpRequest.create(),
                    MockHttpResponse.create()));

            verify(stream).println("bar");
        } finally {
            System.setOut(original);
        }
    }

}
