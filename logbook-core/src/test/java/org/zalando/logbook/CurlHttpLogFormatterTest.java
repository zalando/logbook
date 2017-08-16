package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static java.time.Duration.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CurlHttpLogFormatterTest {

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

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(curl, is("c9408eaa-677d-11e5-9457-10ddb1ee7671 " +
                "curl -v -X GET 'http://localhost/test?limit=1' -H 'Accept: application/json' -H 'Content-Type: text/plain' --data-binary 'Hello, world!'"));
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(MockHeaders.of("Accept", "application/json"));

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(curl, is("0eae9f6c-6824-11e5-8b0a-10ddb1ee7671 " +
                "curl -v -X GET 'http://localhost/test' -H 'Accept: application/json'"));
    }

    @Test
    void shouldEscape() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("char='")
                .withHeaders(MockHeaders.of(
                        "Foo'Bar", "Baz"
                ))
                .withBodyAsString("{\"message\":\"Hello, 'world'!\"}");

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(curl, is("c9408eaa-677d-11e5-9457-10ddb1ee7671 " +
                "curl -v -X GET 'http://localhost/test?char=\\'' -H 'Foo\\'Bar: Baz' --data-binary '{\"message\":\"Hello, \\'world\\'!\"}'"));
    }

    @Test
    void shouldDelegateLogResponse() throws IOException {
        final HttpLogFormatter fallback = mock(HttpLogFormatter.class);
        final HttpLogFormatter unit = new CurlHttpLogFormatter(fallback);

        final MockHttpRequest request = MockHttpRequest.create();
        final MockHttpResponse response = MockHttpResponse.create();

        final Correlation<HttpRequest, HttpResponse> correlation = new SimpleCorrelation<>(
                "3881ae92-6824-11e5-921b-10ddb1ee7671", ZERO, request, response, request, response);

        unit.format(correlation);

        verify(fallback).format(correlation);
    }

}
