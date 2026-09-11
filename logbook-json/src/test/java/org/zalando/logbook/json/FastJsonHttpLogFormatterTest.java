package org.zalando.logbook.json;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.*;
import org.zalando.logbook.test.MockHttpRequest;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.UUID;

import static java.time.Clock.systemUTC;
import static java.time.Instant.MIN;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.zalando.logbook.Origin.REMOTE;

public class FastJsonHttpLogFormatterTest {
    private final JsonMapper jsonMapper;
    private final HttpRequest request = MockHttpRequest.create()
            .withProtocolVersion("HTTP/1.0")
            .withOrigin(REMOTE)
            .withPath("/test")
            .withHeaders(HttpHeaders.empty().update("Accept", "application/json"))
            .withContentType("application/json")
            .withBodyAsString("{\"action\": \"test\"}");

    public FastJsonHttpLogFormatterTest() {
        jsonMapper = JsonMapper.builder().build();
    }

    @Test
    public void shouldNotContainDuplicatedKeys() throws IOException {
        var formatter = new FastJsonHttpLogFormatter(jsonMapper);

        var json = formatter.format(new SimplePrecorrelation(UUID.randomUUID().toString(), systemUTC()), request);

        assertDoesNotThrow(() -> jsonMapper.readTree(json));
    }

    @Test
    public void shouldWriteTimestampAsPojoProperty() throws IOException {
        var formatter = new FastJsonHttpLogFormatter(jsonMapper, new SimpleJsonFieldWriter());

        var clock = Clock.fixed(Instant.parse("2026-09-05T09:54:00Z"), UTC);

        var json = formatter.format(new SimplePrecorrelation(UUID.randomUUID().toString(), clock), request);

        var node = jsonMapper.readTree(json);

        assertEquals("2026-09-05T09:54:00Z", node.get("timestamp").asString());
    }

    @Getter
    @RequiredArgsConstructor
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
    }

    private static class SimpleJsonFieldWriter implements JsonFieldWriter {
        @Override
        public <M extends HttpMessage> void write(M message, JsonGenerator generator) {
        }

        @Override
        public void write(Precorrelation correlation, HttpRequest request, JsonGenerator generator) throws IOException {
            JsonFieldWriter.super.write(correlation, request, generator);
            generator.writePOJOProperty("timestamp", correlation.getStart());
        }
    }
}
