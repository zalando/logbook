package org.zalando.logbook.ecs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EcsStructuredHttpLogFormatterTest {

    private final EcsStructuredHttpLogFormatter ecsStructuredHttpLogFormatter = new EcsStructuredHttpLogFormatter(new ObjectProvider<>() {
        @Override
        public LogbookProperties getObject() throws BeansException {
            return new LogbookProperties();
        }
    });

    @Test
    void shouldPrepareHttpRequestContent() throws IOException {
        // given
        Precorrelation precorrelation = new SimplePrecorrelation("c9408eaa-677d-11e5-9457-10ddb1ee7671", Clock.systemUTC());
        HttpRequest httpRequest = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.of("Referer", "http://referrer.org"))
                .withBodyAsString("Hello, world!");

        // when
        Map<String, Object> content = ecsStructuredHttpLogFormatter.prepare(precorrelation, httpRequest);

        // then
        assertThat(content).isNotEmpty();
    }

    @Test
    void shouldPrepareHttpResponseContent() throws IOException {
        // given
        Correlation correlation = new SimpleCorrelation("c9408eaa-677d-11e5-9457-10ddb1ee7671", Instant.now(), Instant.now().plusSeconds(5));
        HttpResponse httpResponse = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withBodyAsString("{\"success\":true}");

        // when
        Map<String, Object> content = ecsStructuredHttpLogFormatter.prepare(correlation, httpResponse);

        // then
        assertThat(content).isNotEmpty();
    }

    @Getter
    private static class SimplePrecorrelation implements Precorrelation {

        private final String id;
        private final Clock clock;
        private final Instant start;

        SimplePrecorrelation(String id, Clock clock) {
            this.id = id;
            this.clock = clock;
            this.start = Instant.now(clock);
        }

        @Override
        public Correlation correlate() {
            Instant end = Instant.now(clock);
            Duration duration = Duration.between(start, end);
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

        SimpleCorrelation(String id, Instant start, Instant end) {
            this(id, start, end, Duration.between(start, end));
        }

    }

}