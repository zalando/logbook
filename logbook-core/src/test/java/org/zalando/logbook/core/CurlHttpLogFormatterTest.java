package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.core.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.core.DefaultLogbook.SimplePrecorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;

import static java.time.Clock.systemUTC;
import static java.time.Instant.MIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

final class CurlHttpLogFormatterTest {

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

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(curl)
                .isEqualTo("c9408eaa-677d-11e5-9457-10ddb1ee7671 " +
                        "curl -v -X GET 'http://localhost/test?limit=1' " +
                        "-H 'Accept: application/json' " +
                        "-H 'Content-Type: text/plain' " +
                        "--data-binary 'Hello, world!'");
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(HttpHeaders.of("Accept", "application/json"));

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(curl)
                .isEqualTo("0eae9f6c-6824-11e5-8b0a-10ddb1ee7671 " +
                        "curl -v -X GET 'http://localhost/test' -H 'Accept: application/json'");
    }

    @Test
    void shouldEscape() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("char='")
                .withHeaders(HttpHeaders.of("Foo'Bar", "Baz"))
                .withBodyAsString("{\"message\":\"Hello, 'world'!\"}");

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation(correlationId, systemUTC()), request);

        assertThat(curl)
                .isEqualTo("c9408eaa-677d-11e5-9457-10ddb1ee7671 " +
                        "curl -v -X GET 'http://localhost/test?char=\\'' " +
                        "-H 'Foo\\'Bar: Baz' " +
                        "--data-binary '{\"message\":\"Hello, \\'world\\'!\"}'");
    }

    @Test
    void shouldDelegateLogResponse() throws IOException {
        final HttpLogFormatter fallback = mock(HttpLogFormatter.class);
        final HttpLogFormatter unit = new CurlHttpLogFormatter(fallback);

        final MockHttpResponse response = MockHttpResponse.create();

        final Correlation correlation = new SimpleCorrelation("3881ae92-6824-11e5-921b-10ddb1ee7671", MIN, MIN);

        unit.format(correlation, response);
        verify(fallback).format(correlation, response);
    }

}
