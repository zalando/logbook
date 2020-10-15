package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

final class HttpLogWriterTest {

    @Test
    void shouldBeActiveByDefault() {
        final HttpLogWriter unit = spy(HttpLogWriter.class);

        assertThat(unit.isActive()).isTrue();
    }

}
