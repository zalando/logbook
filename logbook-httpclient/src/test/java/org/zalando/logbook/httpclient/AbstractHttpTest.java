package org.zalando.logbook.httpclient;

import com.github.restdriver.clientdriver.ClientDriver;
import com.github.restdriver.clientdriver.ClientDriverFactory;
import com.github.restdriver.clientdriver.ClientDriverResponse;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class AbstractHttpTest {

    final ClientDriver driver = new ClientDriverFactory().createClientDriver();

    final HttpLogWriter writer = mock(HttpLogWriter.class);

    @BeforeEach
    void defaultBehaviour() {
        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET http://localhost:%d HTTP/1.1", driver.getPort()))
                .doesNotContain("Content-Type", "Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/").withMethod(POST)
                .withBody("Hello, world!", "text/plain"), giveEmptyResponse());

        sendAndReceive("Hello, world!");

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(
                        format("POST http://localhost:%d HTTP/1.1", driver.getPort()),
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

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 204 No Content")
                .doesNotContain("Content-Type", "Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/").withMethod(POST),
                giveResponse("Hello, world!", "text/plain"));

        final HttpResponse response = sendAndReceive("Hello, world!");

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
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

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    private void sendAndReceive() throws InterruptedException, ExecutionException, IOException {
        sendAndReceive(null);
    }


    @Test
    void shouldNotThrowExceptionWhenLogbookRequestInterceptorHasException() throws IOException, ExecutionException, InterruptedException {
        doThrow(new IOException("Writing request went wrong")).when(writer).write(any(Precorrelation.class), any());

        driver.addExpectation(onRequestTo("/").withMethod(GET), new ClientDriverResponse().withStatus(500));

        sendAndReceive();

        verify(writer).write(any(Precorrelation.class), any());
        verify(writer, never()).write(any(Correlation.class), any());
    }


    @Test
    void shouldNotThrowExceptionWhenLogbookResponseInterceptorHasException() throws IOException, ExecutionException, InterruptedException {
        doThrow(new IOException("Writing response went wrong")).when(writer).write(any(Correlation.class), any());

        driver.addExpectation(onRequestTo("/").withMethod(GET), new ClientDriverResponse().withStatus(500));

        sendAndReceive();

        verify(writer).write(any(Precorrelation.class), any());
        verify(writer).write(any(Correlation.class), any());
    }

    protected abstract HttpResponse sendAndReceive(@Nullable String body)
            throws IOException, ExecutionException, InterruptedException;
}
