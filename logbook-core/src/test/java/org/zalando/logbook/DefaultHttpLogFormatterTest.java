package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static java.time.Duration.ofMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class DefaultHttpLogFormatterTest {

    private final HttpLogFormatter unit = new DefaultHttpLogFormatter();

    @Test
    void shouldLogRequest() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "text/plain"))
                .withBodyAsString("Hello, world!");

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Incoming Request: c9408eaa-677d-11e5-9457-10ddb1ee7671\n" +
                "GET http://localhost/test?limit=1 HTTP/1.0\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!"));
    }

    @Test
    void shouldLogRequestWithoutQueryParameters() throws IOException {
        final String correlationId = "2bd05240-6827-11e5-bbee-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withOrigin(Origin.LOCAL)
                .withPath("/test")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "text/plain"))
                .withBodyAsString("Hello, world!");

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Outgoing Request: 2bd05240-6827-11e5-bbee-10ddb1ee7671\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!"));
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(MockHeaders.of("Accept", "application/json"));

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Incoming Request: 0eae9f6c-6824-11e5-8b0a-10ddb1ee7671\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json"));
    }

    @Test
    void shouldLogResponse() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withHeaders(MockHeaders.of("Content-Type", "application/json"))
                .withBodyAsString("{\"success\":true}");

        final String http = unit.format(new SimpleCorrelation<>(correlationId, ofMillis(125), request, response,
                request, response));

        assertThat(http, is("Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "Duration: 125 ms\n" +
                "HTTP/1.0 201 Created\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"success\":true}"));
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "3881ae92-6824-11e5-921b-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = MockHttpResponse.create()
                .withOrigin(Origin.LOCAL)
                .withStatus(400)
                .withHeaders(MockHeaders.of("Content-Type", "application/json"));

        final String http = unit.format(new SimpleCorrelation<>(correlationId, ofMillis(100), request, response,
                request, response));

        assertThat(http, is("Outgoing Response: 3881ae92-6824-11e5-921b-10ddb1ee7671\n" +
                "Duration: 100 ms\n" +
                "HTTP/1.1 400 Bad Request\n" +
                "Content-Type: application/json"));
    }

}
