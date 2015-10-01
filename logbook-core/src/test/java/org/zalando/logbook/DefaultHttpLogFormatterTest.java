package org.zalando.logbook;

import org.junit.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public final class DefaultHttpLogFormatterTest {

    private final HttpLogFormatter unit = new DefaultHttpLogFormatter();

    @Test
    public void shouldLogRequest() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.builder().build();

        final String http = unit.format(new SimplePrecorrelation(correlationId, request));

        assertThat(http, equalTo("GET /test?limit=1 HTTP/1.1\n" +
                "Accept: application/json\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "Hello, world!"));
    }

    @Test
    public void shouldLogResponse() throws IOException {
        final String correlationId = "2d51bc02-677e-11e5-8b9b-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create();
        final HttpResponse response = MockHttpResponse.create();

        final String http = unit.format(new SimpleCorrelation(correlationId, request, response));

        assertThat(http, equalTo("HTTP/1.1 200\n" +
                "Content-Type: application/json\n" +
                "\n" +
                "{\"success\":true}"));
    }

}