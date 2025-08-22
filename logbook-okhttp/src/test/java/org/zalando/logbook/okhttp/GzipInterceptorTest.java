package org.zalando.logbook.okhttp;

import com.github.tomakehurst.wiremock.WireMockServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class GzipInterceptorTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Logbook logbook = Logbook.builder()
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new LogbookInterceptor(logbook))
            .addNetworkInterceptor(new GzipInterceptor())
            .build();

    private final WireMockServer server = new WireMockServer(options().dynamicPort());

    @BeforeEach
    void defaultBehaviour() {
        server.start();
        when(writer.isActive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse()
                        .withStatus(200)
                        .withBody(toByteArray(getResource("response.txt.gz").openStream()))
                        .withHeader("Content-Encoding", "gzip")
                        .withHeader("Content-Type", "text/plain"))

        );

        execute();
    }

    @Test
    void shouldLogUncompressedResponseBodyAsIs() throws IOException {
        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        execute();
    }

    private void execute() throws IOException {
        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("Hello, world!");
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(), captor.capture());
        return captor.getValue();
    }

}
