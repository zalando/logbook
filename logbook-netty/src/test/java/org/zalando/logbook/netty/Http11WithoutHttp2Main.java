package org.zalando.logbook.netty;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import java.io.IOException;
import java.util.List;


public final class Http11WithoutHttp2Main {

    private Http11WithoutHttp2Main() {
    }

    public static void main(final String[] args) {
        assertUnreachable("io.netty.handler.codec.http2.Http2StreamChannel");
        assertUnreachable("io.netty.handler.codec.http2.HttpConversionUtil");
        assertUnreachable("reactor.netty.http.client.HttpClient");
        assertUnreachable("reactor.netty.http.server.HttpServer");
        final CapturingWriter clientOutput = new CapturingWriter();
        final CapturingWriter serverOutput = new CapturingWriter();

        logClient(logbook(clientOutput));
        logServer(logbook(serverOutput));
        assertContains(clientOutput.requests, "Outgoing Request:", "GET http://unknown/http11 HTTP/1.1");
        assertContains(clientOutput.responses, "Incoming Response:", "HTTP/1.1 200 OK");
        assertContains(serverOutput.requests, "Incoming Request:", "GET http://unknown/http11 HTTP/1.1");
        assertContains(serverOutput.responses, "Outgoing Response:", "HTTP/1.1 200 OK");
        System.out.println("HTTP11_WITHOUT_HTTP2_SUCCESS");
    }

    private static Logbook logbook(final CapturingWriter output) {
        return Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), output))
                .build();
    }

    private static void logClient(final Logbook logbook) {
        final EmbeddedChannel channel = new EmbeddedChannel(new LogbookClientHandler(logbook));
        channel.writeOutbound(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/http11"));
        channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
        channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    private static void logServer(final Logbook logbook) {
        final EmbeddedChannel channel = new EmbeddedChannel(new LogbookServerHandler(logbook));
        channel.writeInbound(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/http11"));
        channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    private static void assertUnreachable(final String className) {
        try {
            Class.forName(className);
            throw new AssertionError(className + " must be unreachable");
        } catch (final ClassNotFoundException ignored) {
            // Expected: this subprocess models an HTTP/1.1-only consumer.
        }
    }

    private static void assertContains(final List<String> output, final String... expected) {
        for (final String literal : expected) {
            if (output.stream().noneMatch(message -> message.contains(literal))) {
                throw new AssertionError("Missing output: " + literal + " in " + output);
            }
        }
    }

    private static final class CapturingWriter implements HttpLogWriter {

        private final List<String> requests = new java.util.ArrayList<>();
        private final List<String> responses = new java.util.ArrayList<>();

        @Override
        public void write(final Precorrelation correlation, final String request) throws IOException {
            requests.add(request);
        }

        @Override
        public void write(final Correlation correlation, final String response) throws IOException {
            responses.add(response);
        }
    }
}
