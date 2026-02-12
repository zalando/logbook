package org.zalando.logbook.json;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.UUID;

import static java.time.Clock.systemUTC;
import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.zalando.logbook.Origin.REMOTE;

public class FastJsonHttpLogFormatterTest {
    private final JsonMapper jsonMapper;
    private final FastJsonHttpLogFormatter formatter;

    public FastJsonHttpLogFormatterTest() {
        jsonMapper = new JsonMapper();
        formatter = new FastJsonHttpLogFormatter(jsonMapper);
    }

    @Test
    public void shouldNotContainDuplicatedKeys() throws IOException {
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withHeaders(HttpHeaders.empty().update("Accept", "application/json"))
                .withContentType("application/json")
                .withBodyAsString("{\"action\": \"test\"}");

        String json = formatter.format(new SimplePrecorrelation(UUID.randomUUID().toString(), systemUTC()), request);

        assertDoesNotThrow(() -> jsonMapper.readTree(json));
    }

    @Getter
    static class SimplePrecorrelation implements Precorrelation {

        private final String id;
        private final Clock clock;
        private final Instant start;

        SimplePrecorrelation(final String id, final Clock clock) {
            this.id = id;
            this.clock = clock;
            this.start = Instant.now(clock);
        }

        @Override
        public Correlation correlate() {
            final Instant end = Instant.now(clock);
            final Duration duration = Duration.between(start, end);
            return new SimpleCorrelation(id, start, end, duration);
        }

    }

    @Getter
    @AllArgsConstructor
    private static class SimpleCorrelation implements Correlation {

        private final String id;
        private final Instant start;
        private final Instant end;
        private final Duration duration;

        SimpleCorrelation(final String id, final Duration duration) {
            this(id, MIN, MIN.plus(duration), duration);
        }

    }
}
