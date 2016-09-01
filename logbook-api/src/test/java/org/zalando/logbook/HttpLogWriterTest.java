package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public final class HttpLogWriterTest {

    @Test
    public void shouldBeActiveByDefault() throws IOException {
        final HttpLogWriter unit = spy(HttpLogWriter.class);

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

}
