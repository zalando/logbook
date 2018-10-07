package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;
import java.time.Duration;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.zalando.logbook.MockHttpResponse.create;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

class KeyValueHttpLogFormatterTest {

    private final HttpLogFormatter unit = new KeyValueHttpLogFormatter();

    @Test
    void shouldLogCompleteRequest() throws IOException {
        final String correlationId = "3ce91230-677b-11e5-87b7-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withMethod("POST")
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "application/json",
                        "Date", "Tue, 15 Nov 1994 08:12:31 GMT"
                ))
                .withContentType("application/xml")
                .withBodyAsString("<action>test</action>");

        final String format = unit.format(correlation(correlationId, request));

        assertThat(format, stringContainsInOrder(
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
        ));
    }

    @Test
    void shouldLogRequestWithoutHeaders() throws IOException {
        final String correlationId = "b7e7a488-682a-11e5-b527-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withBodyAsString("Hello, world!");

        final String format = unit.format(correlation(correlationId, request));

        assertThat(format, not(containsString("headers")));
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

        final String format = unit.format(correlation(correlationId, request));

        assertThat(format, stringContainsInOrder(
                "origin=remote",
                "type=request",
                "correlation=3ce91230-677b-11e5-87b7-10ddb1ee7671",
                "protocol=HTTP/1.0",
                "remote=127.0.0.1",
                "method=POST",
                "uri=http://localhost/test",
                "body=Hello"
        ));
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "ac5c3dc2-682a-11e5-83cd-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create().withBodyAsString("");

        final String format = unit.format(correlation(correlationId, request));

        assertThat(format, not(containsString("body")));
    }

    @Test
    void shouldLogCompleteResponse() throws IOException {
        final String correlationId = "53de2640-677d-11e5-bc84-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(LOCAL)
                .withHeaders(MockHeaders.of("Date", "Tue, 15 Nov 1994 08:12:31 GMT"))
                .withContentType("application/xml")
                .withBodyAsString("<success>true<success>");

        final String format = unit.format(correlation(correlationId, ofMillis(125), request, response));

        assertThat(format, stringContainsInOrder(
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
        ));
    }

    @Test
    void shouldLogResponseWithoutHeaders() throws IOException {
        final String correlationId = "f53ceee2-682a-11e5-a63e-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = create();

        final String format = unit.format(correlation(correlationId, ZERO, request, response));

        assertThat(format, not(containsString("headers")));
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "f238536c-682a-11e5-9bdd-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = create()
                .withBodyAsString("");

        final String format = unit.format(correlation(correlationId, ZERO, request, response));

        assertThat(format, not(containsString("body")));
    }

    private SimplePrecorrelation<HttpRequest> correlation(
            final String correlationId,
            final HttpRequest request
    ) {
        return new SimplePrecorrelation<>(correlationId, request, request);
    }

    private SimpleCorrelation<HttpRequest, HttpResponse> correlation(
            final String correlationId,
            final Duration duration,
            final HttpRequest request,
            final HttpResponse response
    ) {
        return new SimpleCorrelation<>(correlationId, duration, request, response, request, response);
    }
}