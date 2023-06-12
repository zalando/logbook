package org.zalando.logbook.okhttp;

import com.github.restdriver.clientdriver.ClientDriver;
import com.github.restdriver.clientdriver.ClientDriverFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.HEAD;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.lang.String.format;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private final ClientDriver driver = new ClientDriverFactory().createClientDriver();

    @BeforeEach
    void defaultBehaviour() {
        when(writer.isActive()).thenCallRealMethod();
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d/ HTTP/1.1", driver.getPort()))
                .doesNotContainIgnoringCase("Content-Type")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(POST)
                .withBody("Hello, world!", "text/plain"), giveEmptyResponse());

        client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .post(create("Hello, world!", parse("text/plain")))
                .build()).execute();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST http://localhost:%d/ HTTP/1.1", driver.getPort()))
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

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseForNotModified() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET),
                giveEmptyResponse().withStatus(304));

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
        driver.addExpectation(onRequestTo("/").withMethod(HEAD), giveEmptyResponse());

        client.newCall(new Request.Builder()
                .method("HEAD", null)
                .url(driver.getBaseUrl())
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
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

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
        driver.addExpectation(onRequestTo("/").withMethod(GET),
                giveResponse("Hello, world!", "text/plain"));

        final Response response = client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .containsIgnoringCase("Content-Type")
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

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldIgnoreBodies() throws IOException {
        driver.addExpectation(
                onRequestTo("/")
                        .withMethod(POST)
                        .withBody("Hello, world!", "text/plain"),
                giveResponse("Hello, world!", "text/plain"));

        final Response response = client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .addHeader("Ignore", "true")
                .post(create("Hello, world!", parse("text/plain")))
                .build()).execute();

        assertThat(response.body().string()).isEqualTo("Hello, world!");

        {
            final String message = captureRequest();

            assertThat(message)
                    .startsWith("Outgoing Request:")
                    .contains(format("POST http://localhost:%d/ HTTP/1.1", driver.getPort()))
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

    private void sendAndReceive() throws IOException {
        client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .build()).execute();
    }

}
