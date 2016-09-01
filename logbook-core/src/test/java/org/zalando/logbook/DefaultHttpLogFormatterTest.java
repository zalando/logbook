package org.zalando.logbook;

import org.junit.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.MockHttpRequest.request;
import static org.zalando.logbook.MockHttpResponse.response;

public final class DefaultHttpLogFormatterTest {

    private final HttpLogFormatter unit = new DefaultHttpLogFormatter();

    @Test
    public void shouldLogRequest() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = request()
                .protocolVersion("HTTP/1.0")
                .origin(Origin.REMOTE)
                .path("/test")
                .query("limit=1")
                .headers(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "text/plain"))
                .body("Hello, world!")
                .build();

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Incoming Request: c9408eaa-677d-11e5-9457-10ddb1ee7671\n" +
                "GET http://localhost/test?limit=1 HTTP/1.0\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!"));
    }

    @Test
    public void shouldLogRequestWithoutQueryParameters() throws IOException {
        final String correlationId = "2bd05240-6827-11e5-bbee-10ddb1ee7671";
        final HttpRequest request = request()
                .origin(Origin.LOCAL)
                .path("/test")
                .headers(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "text/plain"))
                .body("Hello, world!")
                .build();

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Outgoing Request: 2bd05240-6827-11e5-bbee-10ddb1ee7671\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!"));
    }

    @Test
    public void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = request()
                .path("/test")
                .headers(MockHeaders.of("Accept", "application/json"))
                .build();

        final String http = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(http, is("Incoming Request: 0eae9f6c-6824-11e5-8b0a-10ddb1ee7671\n" +
                "GET http://localhost/test HTTP/1.1\n" +
                "Accept: application/json"));
    }

    @Test
    public void shouldLogResponse() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .protocolVersion("HTTP/1.0")
                .origin(Origin.REMOTE)
                .headers(MockHeaders.of("Content-Type", "application/json"))
                .body("{\"success\":true}")
                .build();

        final String http = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        assertThat(http, is("Incoming Response: 2d51bc02-677e-11e5-8b9b-10ddb1ee7671\n" +
                "HTTP/1.0 200\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"success\":true}"));
    }

    @Test
    public void shouldLogResponseWithoutBody() throws IOException {
        final String correlationId = "3881ae92-6824-11e5-921b-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = response()
                .origin(Origin.LOCAL)
                .headers(MockHeaders.of("Content-Type", "application/json"))
                .build();

        final String http = unit.format(new SimpleCorrelation<>(correlationId, request, response));

        assertThat(http, is("Outgoing Response: 3881ae92-6824-11e5-921b-10ddb1ee7671\n" +
                "HTTP/1.1 200\n" +
                "Content-Type: application/json"));
    }

}
