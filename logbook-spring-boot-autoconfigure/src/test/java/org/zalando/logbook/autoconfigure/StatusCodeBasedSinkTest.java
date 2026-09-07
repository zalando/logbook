package org.zalando.logbook.autoconfigure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Sink;
import org.zalando.logbook.core.ChunkingSink;
import org.zalando.logbook.core.HttpStatusCodeBasedSink;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeBasedSinkTest {

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = true")
    class EnabledProperty {

        @Autowired
        private Sink sink;

        @Test
        void shouldUseStatusCodeBasedSink() {
            assertThat(sink).isInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = false")
    class DisabledProperty {

        @Autowired
        private Sink sink;

        @Test
        void shouldNotUseStatusCodeBasedSink() {
            assertThat(sink).isNotInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }

    @Nested
    @LogbookTest
    class AbsentProperty {

        @Autowired
        private Sink sink;

        @Test
        void shouldNotUseStatusCodeBasedSink() {
            assertThat(sink).isNotInstanceOf(HttpStatusCodeBasedSink.class);
        }
    }

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = true", imports = Config.class)
    class EnabledPropertyWithCustomSink {

        @Autowired
        private Sink sink;

        @Test
        void shouldBackoffAndUseCustomSinkWhenStatusCodeBasedEnabled() {
            assertThat(sink).isInstanceOf(Config.CustomSink.class);
        }
    }

    @Nested
    @LogbookTest(properties = "logbook.write.status-code-based = false", imports = Config.class)
    class DisabledPropertyWithCustomSink {

        @Autowired
        private Sink sink;

        @Test
        void shouldPreferCustomSinkIfStatusCodeBasedDisabled() {
            assertThat(sink).isInstanceOf(Config.CustomSink.class);
        }
    }

    @Nested
    @LogbookTest(properties = {"logbook.write.status-code-based = true", "logbook.write.chunk-size = 100"})
    class EnabledPropertyWithChunkingSink {

        @Autowired
        private List<Sink> sinks;

        @Test
        void shouldWrapStatusCodeBasedAsDelegateForChunkingSink() {
            assertThat(sinks)
                    .hasSize(2)
                    .hasOnlyElementsOfTypes(HttpStatusCodeBasedSink.class, ChunkingSink.class);
        }
    }

    @TestConfiguration
    static class Config {

        static class CustomSink implements Sink {

            @Override
            public void write(Precorrelation precorrelation, HttpRequest request) throws IOException {
                // For testing
            }

            @Override
            public void write(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException {
                // For testing
            }
        }

        @Bean
        Sink customSink() {
            return new CustomSink();
        }
    }
}
