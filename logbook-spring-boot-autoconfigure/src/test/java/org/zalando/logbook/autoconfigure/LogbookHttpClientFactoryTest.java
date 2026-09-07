package org.zalando.logbook.autoconfigure;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.jdkhttpclient.LogbookHttpClient;
import org.zalando.logbook.jdkhttpclient.LogbookHttpClientFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

class LogbookHttpClientFactoryTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LogbookAutoConfiguration.JdkHttpClientAutoConfiguration.class);
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createsFactoryWhenJdkHttpClientIntegrationIsAvailable() {
        contextRunner.withBean(Logbook.class, Logbook::create)
                .run(context -> assertThat(context).hasSingleBean(LogbookHttpClientFactory.class));
    }

    @Test
    void doesNotCreateFactoryWithoutJdkHttpClientIntegration() {
        contextRunner.withClassLoader(new FilteredClassLoader(LogbookHttpClient.class))
                .withBean(Logbook.class, Logbook::create)
                .run(context -> assertThat(context).doesNotHaveBean(LogbookHttpClientFactory.class));
    }

    @Test
    void backsOffForApplicationDefinedFactory() {
        final LogbookHttpClientFactory factory = new LogbookHttpClientFactory(Logbook.create(), false);

        contextRunner.withBean(LogbookHttpClientFactory.class, () -> factory)
                .run(context -> assertThat(context).getBean(LogbookHttpClientFactory.class).isSameAs(factory));
    }

    @Test
    void logsDecodedGzipResponseWhenConfigured() throws Exception {
        final CapturingWriter writer = new CapturingWriter();
        startGzipServer();

        contextRunner.withBean(Logbook.class, () -> logbook(writer))
                .withPropertyValues("logbook.jdkhttpclient.decompress-response=true")
                .run(context -> send(context.getBean(LogbookHttpClientFactory.class)));

        assertThat(writer.messages).anySatisfy(message -> assertThat(message).contains("decoded response"));
    }

    @Test
    void doesNotLogDecodedGzipResponseByDefault() throws Exception {
        final CapturingWriter writer = new CapturingWriter();
        startGzipServer();

        contextRunner.withBean(Logbook.class, () -> logbook(writer))
                .run(context -> send(context.getBean(LogbookHttpClientFactory.class)));

        assertThat(writer.messages).noneSatisfy(message -> assertThat(message).contains("decoded response"));
    }

    private void startGzipServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            final byte[] body = gzip("decoded response");
            exchange.getResponseHeaders().add("Content-Encoding", "gzip");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    private void send(final LogbookHttpClientFactory factory) {
        final HttpClient client = factory.create();
        final HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.getAddress().getPort()))
                .GET().build();
        final HttpResponse<String> response;
        try {
            response = client.send(request, ofString());
        } catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        assertThat(response.body()).isNotEmpty();
    }

    private static byte[] gzip(final String body) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return output.toByteArray();
    }

    private static Logbook logbook(final HttpLogWriter writer) {
        return Logbook.builder().sink(new DefaultSink(new DefaultHttpLogFormatter(), writer)).build();
    }

    private static final class CapturingWriter implements HttpLogWriter {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void write(final Precorrelation precorrelation, final String request) {
            messages.add(request);
        }

        @Override
        public void write(final Correlation correlation, final String response) {
            messages.add(response);
        }
    }
}
