package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public final class HttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() throws IOException {
        final HttpLogWriter unit = spy(HttpLogWriter.class);

        assertThat(unit.isActive(mock(RawHttpRequest.class)), is(true));
    }

}
