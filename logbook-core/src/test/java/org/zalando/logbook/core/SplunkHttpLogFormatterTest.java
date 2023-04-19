package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Instant.MIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;
import static org.zalando.logbook.test.MockHttpResponse.create;

class SplunkHttpLogFormatterTest {

    private final HttpLogFormatter unit = new SplunkHttpLogFormatter();

    @Test
    void shouldLogCompleteRequest() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withMethod("POST")
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Content-Type", "application/json")
                        .update("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/xml")
                .withBodyAsString("<action>test</action>");

        final String format = unit.format(correlation(correlationId), request);

        assertThat(format).containsSubsequence(
                "origin=remote",
                "type=request",
                "correlation=3ce91230-677b-11e5-87b7-10ddb1ee7671",
                "protocol=HTTP/1.0",
                "remote=127.0.0.1",
                "method=POST",
                "uri=http://localhost/test?limit=1",
                "headers={",
                "Accept=[application/json]",
                "Content-Type=[application/json]",
                "Date=[Tue, 15 Nov 1994 08:12:31 GMT]",
                "}",
                "body=<action>test</action>"
        );
    }

    @Test
    void shouldLogRequestWithoutHeaders() throws IOException {
        final String correlationId = "b7e7a488-682a-11e5-b527-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withBodyAsString("Hello, world!");

        final String format = unit.format(correlation(correlationId), request);

        assertThat(format).doesNotContain("headers");
    }

    @Test
    void shouldLogRequestWithoutContentType() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withMethod("POST")
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withBodyAsString("Hello");

        final String format = unit.format(correlation(correlationId), request);

        assertThat(format).containsSubsequence(
                "origin=remote",
                "type=request",
                "correlation=3ce91230-677b-11e5-87b7-10ddb1ee7671",
                "protocol=HTTP/1.0",
                "remote=127.0.0.1",
                "method=POST",
                "uri=http://localhost/test",
                "body=Hello"
        );
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "ac5c3dc2-682a-11e5-83cd-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create().withBodyAsString("");

        final String format = unit.format(correlation(correlationId), request);

        assertThat(format).doesNotContain("body");
    }

    @Test
    void shouldLogCompleteResponse() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpResponse response = create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(LOCAL)
                .withHeaders(HttpHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/xml")
                .withBodyAsString("<success>true<success>");

        final String format = unit.format(correlation(correlationId, ofMillis(125)), response);

        assertThat(format).containsSubsequence(
                "origin=local",
                "type=response",
                "correlation=53de2640-677d-11e5-bc84-10ddb1ee7671",
                "duration=125",
                "protocol=HTTP/1.0",
                "status=200",
                "headers={",
                "Date=[Tue, 15 Nov 1994 08:12:31 GMT]",
                "}",
                "body=<success>true<success>"
        );
    }

    @Test
    void shouldLogResponseWithoutHeaders() throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpResponse response = create();

        final String format = unit.format(correlation(correlationId, ZERO), response);

        assertThat(format).doesNotContain("headers");
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpResponse response = create()
                .withBodyAsString("");

        final String format = unit.format(correlation(correlationId, ZERO), response);

        assertThat(format).doesNotContain("body");
    }

    private SimplePrecorrelation correlation(final String correlationId) {
        return new SimplePrecorrelation(correlationId, Clock.systemUTC());
    }

    private SimpleCorrelation correlation(
            final String correlationId,
            final Duration duration) {
        return new SimpleCorrelation(correlationId, MIN, MIN.plus(duration));
    }
}
