package org.zalando.logbook.jdkserver;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.TestStrategy;
import org.zalando.logbook.WithoutBodyStrategy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LogbookFilterTest {

    private static final String RESPONSE_WITH_BODY = "Incoming Request: test\n" +
            "Remote: remote\n" +
            "GET http://0.0.0.0:9999/path?query=1&other=2 HTTP/1.1\n" +
            "Header1: h1value1, h1value2\n" +
            "Header2: h2value1\n" +
            "\n" +
            "requestOutgoing Response: test\n" +
            "Duration: X ms\n" +
            "HTTP/1.1 200 OK\n" +
            "Response-header1: h1value1, h1value2\n" +
            "Response-header2: h2value1\n" +
            "\n" +
            "responseok";

    @Test
    public void shouldReturnDescription() {
        assertEquals("Logbook filter", new LogbookFilter().description());
    }

    @Test
    public void shouldDoRequestAndResponseProcessing() throws IOException {
        Filter.Chain chain = new Filter.Chain(Collections.emptyList(), new MockHandler());
        StringLogWriter stringLogWriter = new StringLogWriter();
        LogbookFilter logbookFilter = new LogbookFilter(Logbook.builder()
                .correlationId(request -> "test")
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), stringLogWriter)).build(), new TestStrategy());
        logbookFilter.doFilter(new MockHttpExchange(), chain);
        assertEquals(RESPONSE_WITH_BODY, cleanupDuration(stringLogWriter.getResult()));
    }

    @Test
    public void shouldDoRequestAndResponseProcessingWithoutStrategy() throws IOException {
        Filter.Chain chain = new Filter.Chain(Collections.emptyList(), new MockHandler());
        StringLogWriter stringLogWriter = new StringLogWriter();
        LogbookFilter logbookFilter = new LogbookFilter(Logbook.builder()
                .correlationId(request -> "test")
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), stringLogWriter)).build());
        logbookFilter.doFilter(new MockHttpExchange(), chain);
        assertEquals(RESPONSE_WITH_BODY, cleanupDuration(stringLogWriter.getResult()));
    }

    @Test
    public void shouldDoRequestAndResponseProcessingWithWithoutBodyStrategy() throws IOException {
        Filter.Chain chain = new Filter.Chain(Collections.emptyList(), new MockHandler());
        StringLogWriter stringLogWriter = new StringLogWriter();
        LogbookFilter logbookFilter = new LogbookFilter(Logbook.builder()
                .correlationId(request -> "test")
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), stringLogWriter)).build(), new WithoutBodyStrategy());
        logbookFilter.doFilter(new MockHttpExchange(), chain);
        assertEquals("Incoming Request: test\n" +
                "Remote: remote\n" +
                "GET http://0.0.0.0:9999/path?query=1&other=2 HTTP/1.1\n" +
                "Header1: h1value1, h1value2\n" +
                "Header2: h2value1Outgoing Response: test\n" +
                "Duration: X ms\n" +
                "HTTP/1.1 200 OK\n" +
                "Response-header1: h1value1, h1value2\n" +
                "Response-header2: h2value1", cleanupDuration(stringLogWriter.getResult()));
    }

    private static String cleanupDuration(String log) {
        return log.replaceAll("Duration:\\s[0-9]+\\sms", "Duration: X ms");
    }

    private static class MockHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(200,0);
            OutputStream os = exchange.getResponseBody();
            os.write("response".getBytes(StandardCharsets.UTF_8));
            os.write(new byte[] {'o'}, 0, 1);
            os.write('k');
            os.flush();
            os.close();
        }

    }

    private static class StringLogWriter implements HttpLogWriter {

        private final StringBuilder result = new StringBuilder();

        @Override
        public void write(Precorrelation precorrelation, String request) throws IOException {
            result.append(request);
        }

        @Override
        public void write(Correlation correlation, String response) throws IOException {
            result.append(response);
        }

        public String getResult() {
            return result.toString();
        }

    }

}
