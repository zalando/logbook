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

import java.io.IOException;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.POST;
import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.lang.String.format;
import static okhttp3.MediaType.parse;
import static okhttp3.RequestBody.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookInterceptorTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Logbook logbook = Logbook.builder()
            .writer(writer)
            .build();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new LogbookInterceptor(logbook))
            .build();

    public final ClientDriver driver = new ClientDriverFactory().createClientDriver();

    @BeforeEach
    void defaultBehaviour() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message, startsWith("Outgoing Request:"));
        assertThat(message, containsString(format("GET http://localhost:%d/ HTTP/1.1", driver.getPort())));
        assertThat(message, not(containsStringIgnoringCase("Content-Type")));
        assertThat(message, not(containsString("Hello, world!")));
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(POST)
                .withBody("Hello, world!", "text/plain"), giveEmptyResponse());

        client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .post(create(parse("text/plain"), "Hello, world!"))
                .build()).execute();

        final String message = captureRequest();

        assertThat(message, startsWith("Outgoing Request:"));
        assertThat(message, containsString(format("POST http://localhost:%d/ HTTP/1.1", driver.getPort())));
        assertThat(message, containsStringIgnoringCase("Content-Type: text/plain"));
        assertThat(message, containsString("Hello, world!"));
    }

    @SuppressWarnings("unchecked")
    private String captureRequest() throws IOException {
        final ArgumentCaptor<Precorrelation<String>> captor = ArgumentCaptor.forClass(Precorrelation.class);
        verify(writer).writeRequest(captor.capture());
        return captor.getValue().getRequest();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).writeRequest(any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        final String message = captureResponse();

        assertThat(message, startsWith("Incoming Response:"));
        assertThat(message, containsString("HTTP/1.1 204 No Content"));
        assertThat(message, not(containsStringIgnoringCase("Content-Type")));
        assertThat(message, not(containsString("Hello, world!")));
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET),
                giveResponse("Hello, world!", "text/plain"));

        final Response response = client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .build()).execute();

        assertThat(response.body().string(), is("Hello, world!"));

        final String message = captureResponse();

        assertThat(message, startsWith("Incoming Response:"));
        assertThat(message, containsString("HTTP/1.1 200 OK"));
        assertThat(message, containsStringIgnoringCase("Content-Type: text/plain"));
        assertThat(message, containsString("Hello, world!"));
    }

    @SuppressWarnings("unchecked")
    private String captureResponse() throws IOException {
        final ArgumentCaptor<Correlation<String, String>> captor = ArgumentCaptor.forClass(Correlation.class);
        verify(writer).writeResponse(captor.capture());
        return captor.getValue().getResponse();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException {
        when(writer.isActive(any())).thenReturn(false);

        driver.addExpectation(onRequestTo("/").withMethod(GET), giveEmptyResponse());

        sendAndReceive();

        verify(writer, never()).writeResponse(any());
    }

    private void sendAndReceive() throws IOException {
        client.newCall(new Request.Builder()
                .url(driver.getBaseUrl())
                .build()).execute();
    }

}
