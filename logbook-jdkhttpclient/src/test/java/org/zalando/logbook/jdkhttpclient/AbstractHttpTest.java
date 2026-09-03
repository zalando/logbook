package org.zalando.logbook.jdkhttpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
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
import java.net.http.HttpResponse;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy.NEVER;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class AbstractHttpTest {

    final WireMockServer server = new WireMockServer(options().dynamicPort().gzipDisabled(true));

    final WireMockServer nonChunkedServer = new WireMockServer(options().dynamicPort().gzipDisabled(true)
            .useChunkedTransferEncoding(NEVER));

    final HttpLogWriter writer = mock(HttpLogWriter.class);

    protected final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    @BeforeEach
    void defaultBehaviour() {
        server.start();
        nonChunkedServer.start();
        when(writer.isActive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        server.stop();
        nonChunkedServer.stop();
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException, InterruptedException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d/ HTTP/1.1", server.port()))
                .doesNotContain("Content-Type", "Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException, InterruptedException {
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

    protected String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException, InterruptedException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException, InterruptedException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(204)));

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 204 No Content")
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseWithChunkedBody() throws IOException, InterruptedException {
        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        final HttpResponse<String> response = sendAndReceive("Hello, world!");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException, InterruptedException {
        server.stubFor(post("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")
                .withHeader("Content-Length", "13")));

        final HttpResponse<String> response = sendAndReceive("Hello, world!");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("Hello, world!");
    }

    protected String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException, InterruptedException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotThrowExceptionWhenLogbookRequestInterceptorHasException() throws IOException, InterruptedException {
        doThrow(new IOException("Writing request went wrong")).when(writer).write(any(Precorrelation.class), any());

        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        final HttpResponse<String> response = sendAndReceive();

        assertThat(response.statusCode()).isEqualTo(200);
        verify(writer).write(any(Precorrelation.class), any());
        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotThrowExceptionWhenLogbookResponseInterceptorHasException() throws IOException, InterruptedException {
        doThrow(new IOException("Writing response went wrong")).when(writer).write(any(Correlation.class), any());

        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        final HttpResponse<String> response = sendAndReceive();

        assertThat(response.statusCode()).isEqualTo(200);
        verify(writer).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldHandleLargeChunkedResponseBody() throws IOException, InterruptedException {
        final int size = 80 * 1024;
        final byte[] largeBody = new byte[size];
        Arrays.fill(largeBody, (byte) 'A');

        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(largeBody)));

        final HttpResponse<String> response = sendAndReceive();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).hasSize(size);

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("A".repeat(size));
    }

    @Test
    void shouldHandleLargeNonChunkedResponseBody() throws IOException, InterruptedException {
        final int size = 80 * 1024;
        final byte[] largeBody = new byte[size];
        Arrays.fill(largeBody, (byte) 'A');

        nonChunkedServer.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/plain")
                .withBody(largeBody)));

        final HttpResponse<String> response = sendAndReceive(nonChunkedServer);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).hasSize(size);

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("A".repeat(size));
    }

    private HttpResponse<String> sendAndReceive() throws IOException, InterruptedException {
        return sendAndReceive((String) null);
    }

    protected HttpResponse<String> sendAndReceive(@Nullable final String body) throws IOException, InterruptedException {
        return sendAndReceive(server, body);
    }

    private HttpResponse<String> sendAndReceive(final WireMockServer server) throws IOException, InterruptedException {
        return sendAndReceive(server, null);
    }

    protected abstract HttpResponse<String> sendAndReceive(WireMockServer server, @Nullable String body)
            throws IOException, InterruptedException;
}
