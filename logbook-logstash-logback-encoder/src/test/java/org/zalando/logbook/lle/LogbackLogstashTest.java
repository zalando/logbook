package org.zalando.logbook.lle;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.time.Clock.systemUTC;
import static java.time.Duration.ofMillis;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.zalando.logbook.Origin.REMOTE;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.MockHeaders;
import org.zalando.logbook.MockHttpRequest;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import lombok.AllArgsConstructor;

public class LogbackLogstashTest {

    @AfterAll
    public static void setup() {
        StaticAppender.reset();
        PrettyPrintingStaticAppender.reset();
    }

    @Test
    void shouldLogRequestAndResponse() throws IOException {
        LogbackLogstashLogWriter logWriter = new DefaultLogbackLogstashHttpLogWriter();
        HttpLogFormatter formatter = new JsonHttpLogFormatter();
        LogbackLogstashSink sink = new LogbackLogstashSink(formatter, logWriter);

        assertTrue(logWriter.isActive());
        
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Thomas\"}}");

        sink.write(new SimplePrecorrelation(correlationId, systemUTC()), request);

        for(String last : new String[] {StaticAppender.getLastStatement(), PrettyPrintingStaticAppender.getLastStatement()} ) {
            with(last)
                .assertThat("$.http.origin", is("remote"))
                .assertThat("$.http.type", is("request"))
                .assertThat("$.http.correlation", is(correlationId))
                .assertThat("$.http.protocol", is("HTTP/1.0"))
                .assertThat("$.http.remote", is("127.0.0.1"))
                .assertThat("$.http.method", is("GET"))
                .assertThat("$.http.uri", is("http://localhost/test?limit=1"))
                .assertThat("$.http.headers.*", hasSize(2))
                .assertThat("$.http.headers['Accept']", is(singletonList("application/json")))
                .assertThat("$.http.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.http.body.person.name", is("Thomas"));
        }
        
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withHeaders(MockHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/json")
                .withBodyAsString("{\"person\":{\"name\":\"Magnus\"}}");

        sink.write(new SimpleCorrelation(correlationId, ofMillis(125)), request, response);

        for(String last : new String[] {StaticAppender.getLastStatement(), PrettyPrintingStaticAppender.getLastStatement()} ) {
            with(last)
                .assertThat("$.http.origin", is("remote"))
                .assertThat("$.http.type", is("response"))
                .assertThat("$.http.correlation", is(correlationId))
                .assertThat("$.http.protocol", is("HTTP/1.0"))
                .assertThat("$.http.status", is(200))
                .assertThat("$.http.headers.*", hasSize(1))
                .assertThat("$.http.headers['Date']", is(singletonList("Tue, 15 Nov 1994 08:12:31 GMT")))
                .assertThat("$.http.body.person.name", is("Magnus"))
                .assertThat("$.http.duration", is(125));
        }        
    }

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
        public String getId() {
            return id;
        }

        @Override
        public Correlation correlate() {
            final Instant end = Instant.now(clock);
            final Duration duration = Duration.between(start, end);
            return new SimpleCorrelation(id, duration);
        }

    }

    @AllArgsConstructor
    static class SimpleCorrelation implements Correlation {

        private final String id;
        private final Duration duration;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Duration getDuration() {
            return duration;
        }

    }

}
