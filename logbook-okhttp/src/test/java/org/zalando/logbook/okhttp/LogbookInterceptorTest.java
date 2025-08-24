package org.zalando.logbook.okhttp;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
import java.net.ProtocolException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookInterceptorTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new LogbookInterceptor(logbook))
            .build();

    private final WireMockServer server = new WireMockServer(options().dynamicPort().gzipDisabled(true));

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
        server.stubFor(post("/").withRequestBody(equalTo("Hello, world!")).willReturn(aResponse().withStatus(200)));

        client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .post(create("Hello, world!", parse("text/plain")))
                .build()).execute();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST http://localhost:%d/ HTTP/1.1", server.port()))
                .containsIgnoringCase("Content-Type: text/plain")
                .contains("Hello, world!");
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
    void shouldLogResponseWithBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200)
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
                .containsIgnoringCase("Content-Type")
                .contains("Hello, world!");
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
                .post(create("Hello, world!", parse("text/plain")))
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
                    .containsIgnoringCase("HTTP/1.1 200 OK")
                    .containsIgnoringCase("Content-Type: text/plain")
                    .doesNotContain("Hello, world!");
        }
    }

    @Test
    void shouldNotInterruptRequestProcessingWhenLoggingFails() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        doThrow(new IOException("Writing request went wrong")).when(writer).write(any(Precorrelation.class), any());

        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldNotInterruptResponseProcessingWhenLoggingFails() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

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

    @Test
    void shouldLogResponseWithMalformedChunkedBody() throws IOException {
        server.stubFor(get("/").willReturn(aResponse().withStatus(200)
                .withBody("Hello, world!")
                .withHeader("Content-Type", "text/plain")
                .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        final Response response = client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();

        assertThatThrownBy(() -> response.body().string()).isInstanceOf(ProtocolException.class);

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("!This is not a real response body. Logbook was unable to read the response body!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }
    private void sendAndReceive() throws IOException {
        client.newCall(new Request.Builder()
                .url(server.baseUrl())
                .build()).execute();
    }

}
