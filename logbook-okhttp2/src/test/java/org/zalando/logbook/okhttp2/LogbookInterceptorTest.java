package org.zalando.logbook.okhttp2;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.squareup.okhttp.MediaType.parse;
import static com.squareup.okhttp.RequestBody.create;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookInterceptorTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final OkHttpClient client = new OkHttpClient();

    private final WireMockServer server = new WireMockServer(options().dynamicPort().gzipDisabled(true));

    LogbookInterceptorTest() {
        client.networkInterceptors().add(new LogbookInterceptor(Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build()));
    }

    @BeforeEach
    void defaultBehaviour() {
        server.start();
        when(writer.isActive()).thenCallRealMethod();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d/ HTTP/1.1", server.port()))
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        server.stubFor(post("/")
                .withRequestBody(equalTo("Hello, world!")).willReturn(aResponse().withStatus(204)));

        client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .post(create(parse("text/plain"), "Hello, world!"))
                .build()).execute();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST http://localhost:%d/ HTTP/1.1", server.port()))
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseForNotModified() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(304)));

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 304 Not Modified")
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseForHeadRequest() throws IOException {
        server.stubFor(head(urlEqualTo("/")).willReturn(aResponse().withStatus(204)));

        client.newCall(new Request.Builder()
                .method("HEAD", null)
                .url(server.baseUrl())
                .build()).execute();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 204 No Content")
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200).withHeader("Content-Length", "0")));

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

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
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException {
        when(writer.isActive()).thenReturn(false);

        server.stubFor(get("/").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldIgnoreBodies() throws IOException {
        server.stubFor(post("/").withRequestBody(equalTo("Hello, world!")).willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .addHeader("Ignore", "true")
                .post(create(parse("text/plain"), "Hello, world!"))
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        {
            final String message = captureRequest();

            assertThat(message)
                    .startsWith("Outgoing Request:")
                    .contains(format("POST http://localhost:%d/ HTTP/1.1", server.port()))
                    .containsIgnoringCase("Content-Type: text/plain")
                    .doesNotContain("Hello, world!");
        }

        {
            final String message = captureResponse();

            assertThat(message)
                    .startsWith("Incoming Response:")
                    .contains("HTTP/1.1 200 OK")
                    .containsIgnoringCase("Content-Type: text/plain")
                    .doesNotContain("Hello, world!");
        }
    }

    @Test
    void shouldNotInterruptRequestProcessingWhenLoggingFails() throws IOException {
        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        doThrow(new IOException("Writing request went wrong")).when(writer).write(any(Precorrelation.class), any());

        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotInterruptResponseProcessingWhenLoggingFails() throws IOException {
        server.stubFor(get("/").willReturn(aResponse()
                .withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")));

        doThrow(new IOException("Writing response went wrong")).when(writer).write(any(Correlation.class), any());

        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d/ HTTP/1.1", server.port()))
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");

        assertThat(response.body().string()).isEqualTo("Hello, world!");

    }

    private void sendAndReceive() throws IOException {
        client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();
    }

}
