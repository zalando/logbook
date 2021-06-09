package org.zalando.logbook.httpclient5;

import com.github.restdriver.clientdriver.ClientDriver;
import com.github.restdriver.clientdriver.ClientDriverFactory;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.*;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class AbstractHttpTest {

    final ClientDriver driver = new ClientDriverFactory().createClientDriver();

    final HttpLogWriter writer = mock(HttpLogWriter.class);

    protected final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

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
                .contains(format("GET http://localhost:%d/ HTTP/1.1", driver.getPort()))
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
                        format("POST http://localhost:%d/ HTTP/1.1", driver.getPort()),
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
    void shouldLogResponseWithBody() throws IOException, ExecutionException, InterruptedException, ParseException {
        driver.addExpectation(onRequestTo("/").withMethod(POST),
                giveResponse("Hello, world!", "text/plain"));

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

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    private void sendAndReceive() throws InterruptedException, ExecutionException, IOException {
        sendAndReceive(null);
    }

    protected abstract ClassicHttpResponse sendAndReceive(@Nullable String body)
            throws IOException, ExecutionException, InterruptedException;
}
