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

import java.io.IOException;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static com.google.common.io.Resources.getResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

final class GzipInterceptorTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final Logbook logbook = Logbook.builder()
            .writer(writer)
            .build();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new LogbookInterceptor(logbook))
            .addNetworkInterceptor(new GzipInterceptor())
            .build();

    private final ClientDriver driver = new ClientDriverFactory().createClientDriver();

    @BeforeEach
    void defaultBehaviour() throws IOException {
        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET),
                giveResponseAsBytes(getResource("response.txt.gz").openStream(), "text/plain")
                        .withHeader("Content-Encoding", "gzip"));

        execute();
    }

    @Test
    void shouldLogUncompressedResponseBodyAsIs() throws IOException {
        driver.addExpectation(onRequestTo("/").withMethod(GET),
                giveResponse("Hello, world!", "text/plain"));

        execute();
    }

    private void execute() throws IOException {
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

}
