package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.spy;

public final class HttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() {
        final HttpLogWriter unit = spy(HttpLogWriter.class);

        assertThat(unit.isActive(), is(true));
    }

}
