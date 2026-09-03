package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.logbook.Sink;
import org.zalando.logbook.core.HttpStatusCodeBasedSink;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeBasedSinkTest {

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = true")
    class Enabled {

        @Autowired
        private Sink sink;

        @Test
        void shouldUseStatusCodeBasedSink() {
            assertThat(sink).isInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = false")
    class Disabled {

        @Autowired
        private Sink sink;

        @Test
        void shouldNotUseStatusCodeBasedSink() {
            assertThat(sink).isNotInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }

    @Nested
    @LogbookTest
    class Absent {

        @Autowired
        private Sink sink;

        @Test
        void shouldNotUseStatusCodeBasedSink() {
            assertThat(sink).isNotInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }
}
