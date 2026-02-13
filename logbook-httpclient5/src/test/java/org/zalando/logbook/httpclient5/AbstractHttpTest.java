package org.zalando.logbook.httpclient5;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.httpclient5.RemoteResponseTest.compress;

abstract class AbstractHttpTest {

    final WireMockServer server = new WireMockServer(options().dynamicPort().gzipDisabled(true));

    final HttpLogWriter writer = mock(HttpLogWriter.class);

    protected final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

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
    void shouldLogRequestWithoutBody() throws IOException, ExecutionException, InterruptedException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d/ HTTP/1.1", server.port()))
                .doesNotContain("Content-Type", "Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException, ExecutionException, InterruptedException {
        server.stubFor(post("/").withRequestBody(equalTo("Hello, world!")).willReturn(aResponse().withStatus(204)));

        sendAndReceive("Hello, world!");

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(
                        format("POST http://localhost:%d/ HTTP/1.1", server.port()),
                        "Content-Type: text/plain",
                        "Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException, ExecutionException, InterruptedException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException, ExecutionException, InterruptedException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(204)));

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 204 No Content")
                .doesNotContain("Content-Type", "Hello, world!");
    }

    @Test
    void shouldLogResponseWithChunkedBody() throws IOException, ExecutionException, InterruptedException, ParseException {
        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        final ClassicHttpResponse response = sendAndReceive("Hello, world!");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK", "Content-Type: text/plain", "Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException, ExecutionException, InterruptedException, ParseException {
        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")
                .withHeader("Content-Length", "13")));

        final ClassicHttpResponse response = sendAndReceive("Hello, world!");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK", "Content-Type: text/plain", "Hello, world!");
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException, ExecutionException, InterruptedException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotThrowExceptionWhenLogbookRequestInterceptorHasException() throws IOException, ExecutionException, InterruptedException {
        doThrow(new IOException("Writing request went wrong")).when(writer).write(any(Precorrelation.class), any());

        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        sendAndReceive();

        verify(writer).write(any(Precorrelation.class), any());
        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotThrowExceptionWhenLogbookResponseInterceptorHasException() throws IOException, ExecutionException, InterruptedException {
        doThrow(new IOException("Writing response went wrong")).when(writer).write(any(Correlation.class), any());

        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        sendAndReceive();

        verify(writer).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogDecompressedResponse() throws IOException, ExecutionException, InterruptedException, ParseException {
        String body = "Hello, dude!";
        byte[] compressed = compress(body.getBytes(StandardCharsets.UTF_8));
        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody(compressed)
                .withHeader("Content-Type", "text/plain")
                .withHeader("Content-Encoding", "gzip")));

        final ClassicHttpResponse response = sendAndReceive("Hello, world!");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK", "Content-Type: text/plain", "Hello, dude!");
    }

    private void sendAndReceive() throws InterruptedException, ExecutionException, IOException {
        sendAndReceive(null);
    }

    protected abstract ClassicHttpResponse sendAndReceive(@Nullable String body)
            throws IOException, ExecutionException, InterruptedException;
}
