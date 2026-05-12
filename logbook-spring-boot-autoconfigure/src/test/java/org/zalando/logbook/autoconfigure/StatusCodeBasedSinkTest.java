package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Sink;
import org.zalando.logbook.core.StatusCodeBasedSink;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeBasedSinkTest {

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = true")
    class Enabled {

        @Autowired
        private Sink sink;

        @Test
        void shouldUseStatusCodeBasedSink() {
            assertThat(sink).isInstanceOf(StatusCodeBasedSink.class);
        }

    }

    @Nested
    @LogbookTest
    class Disabled {

        @Autowired
        private Sink sink;

        @Test
        void shouldNotUseStatusCodeBasedSink() {
            assertThat(sink).isNotInstanceOf(StatusCodeBasedSink.class);
        }

    }

}
