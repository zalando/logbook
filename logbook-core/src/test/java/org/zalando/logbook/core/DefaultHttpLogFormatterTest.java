package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;
import org.zalando.logbook.attributes.HttpAttributes;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.Clock.systemUTC;
import static org.assertj.core.api.Assertions.assertThat;

final class DefaultHttpLogFormatterTest {

    private final HttpLogFormatter unit = new DefaultHttpLogFormatter();

    private final Map<String, String> attributesMap = new HashMap<>();
    {
        attributesMap.put("subject", "John");
        attributesMap.put("object", "Window");
    }

    private final HttpAttributes httpAttributes = new HttpAttributes(attributesMap);

    @Test
    void shouldLogRequest() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Content-Type", "text/plain"))
                .withBodyAsString("Hello, world!");

        final String http = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(http).isEqualTo("Incoming Request: c9408eaa-677d-11e5-9457-10ddb1ee7671\n" +
                "Remote: 127.0.0.1\n" +
                "GET http://localhost/test?limit=1 HTTP/1.0\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!");
    }

    @Test
    void shouldLogRequestWithoutQueryParameters() throws IOException {
        final String correlationId = "2bd05240-6827-11e5-bbee-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withOrigin(Origin.LOCAL)
                .withPath("/test")
                .withHeaders(HttpHeaders.empty()
                        .update("Accept", "application/json")
                        .update("Content-Type", "text/plain"))
                .withBodyAsString("Hello, world!");

        final String http = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(http).isEqualTo("Outgoing Request: 2bd05240-6827-11e5-bbee-10ddb1ee7671\n" +
                "Remote: 127.0.0.1\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!");
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(HttpHeaders.of("Accept", "application/json"));

        final String http = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(http).isEqualTo("Incoming Request: 0eae9f6c-6824-11e5-8b0a-10ddb1ee7671\n" +
                "Remote: 127.0.0.1\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json");
    }


    @Test
    void shouldLogRequestWithHttpAttributes() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final MockHttpRequest request1 = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(HttpHeaders.of("Accept", "application/json"))
                .withHttpAttributes(httpAttributes);

        final String http1 = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request1);

        final String expected1 = "Incoming Request: 0eae9f6c-6824-11e5-8b0a-10ddb1ee7671\n" +
                "Remote: 127.0.0.1\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Attributes: {subject=John, object=Window}";

        assertThat(http1).isEqualTo(expected1);

        // Same, but with body:
        final HttpRequest request2 = request1.withBodyAsString("Hello, world!");
        final String http2 = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request2);
        final String expected2 = expected1 + "\n\nHello, world!";
        assertThat(http2).isEqualTo(expected2);
    }

    @Test
    void shouldLogResponse() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"))
                .withBodyAsString("{\"success\":true}");

        final String http = unit.format(new SimpleCorrelation(
                correlationId, Instant.MIN, Instant.MIN.plusMillis(125)), response);

        assertThat(http).isEqualTo("Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "Duration: 125 ms\n" +
                "HTTP/1.0 201 Created\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"success\":true}");
    }

    @Test
    void shouldLogResponseWithHttpAttributes() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final MockHttpResponse response1 = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"))
                .withHttpAttributes(httpAttributes);

        final String http1 = unit.format(new SimpleCorrelation(
                correlationId, Instant.MIN, Instant.MIN.plusMillis(125)), response1);

        final String expected1 = "Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "Duration: 125 ms\n" +
                "HTTP/1.0 201 Created\n" +
                "Content-Type: application/json\n" +
                "Attributes: {subject=John, object=Window}";

        assertThat(http1).isEqualTo(expected1);

        // Same, but with body:
        final MockHttpResponse response2 = response1.withBodyAsString("{\"success\":true}");
        final String http2 = unit.format(new SimpleCorrelation(
                correlationId, Instant.MIN, Instant.MIN.plusMillis(125)), response2);
        final String expected2 = expected1 + "\n\n{\"success\":true}";
        assertThat(http2).isEqualTo(expected2);
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "3881ae92-6824-11e5-921b-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withOrigin(Origin.LOCAL)
                .withStatus(400)
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"));

        final String http = unit.format(new SimpleCorrelation(correlationId,
                Instant.MIN, Instant.MIN.plusMillis(100)), response);

        assertThat(http).isEqualTo("Outgoing Response: 3881ae92-6824-11e5-921b-10ddb1ee7671\n" +
                "Duration: 100 ms\n" +
                "HTTP/1.1 400 Bad Request\n" +
                "Content-Type: application/json");
    }

    @Test
    void shouldLogResponseForUnknownStatusCode() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(1000)
                .withHeaders(HttpHeaders.of("Content-Type", "application/json"))
                .withBodyAsString("{\"success\":true}");

        final String http = unit.format(new SimpleCorrelation(correlationId, Instant.MIN, Instant.MIN.plusMillis(125)),
                response);

        assertThat(http).isEqualTo("Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "Duration: 125 ms\n" +
                "HTTP/1.0 1000\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"success\":true}");
    }

    @Test
    void shouldLogResponseForEmptyHeader() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";

        final HttpResponse response = MockHttpResponse.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withStatus(201)
                .withHeaders(HttpHeaders.empty()
                        .update("Content-Type", "application/json")
                        .update("X-Empty-Header", Collections.emptyList()))
                .withBodyAsString("{\"success\":true}");

        final String http = unit.format(new SimpleCorrelation(correlationId, Instant.MIN, Instant.MIN.plusMillis(125)),
                response);

        assertThat(http).isEqualTo("Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "Duration: 125 ms\n" +
                "HTTP/1.0 201 Created\n" +
                "Content-Type: application/json\n" +
                "X-Empty-Header: \n" +
                "\n" +
                "{\"success\":true}");
    }
}
