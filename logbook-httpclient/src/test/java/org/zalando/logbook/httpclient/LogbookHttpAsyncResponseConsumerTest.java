package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.create;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.createConsumer;
import static org.apache.http.util.EntityUtils.toByteArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public final class LogbookHttpAsyncResponseConsumerTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
            .writer(writer)
            .build();

    private final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .build();

    @SuppressWarnings("unchecked")
    private final FutureCallback<HttpResponse> callback = mock(FutureCallback.class);
    private final Correlator correlator = mock(Correlator.class);

    @BeforeEach
    void start() {
        client.start();
    }

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    protected void sendAndReceive() throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/"),
                giveResponse("Hello, world!", "text/plain"));

        final HttpGet request = new HttpGet(driver.getBaseUrl());

        final HttpResponse response = client.execute(create(request),
                new LogbookHttpAsyncResponseConsumer<>(createConsumer()), callback).get();

        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(new String(toByteArray(response.getEntity()), UTF_8), is("Hello, world!"));
    }

    @Test
    void shouldWrapIOException() throws IOException {
        final HttpAsyncResponseConsumer<HttpResponse> unit = new LogbookHttpAsyncResponseConsumer<>(createConsumer());

        final BasicHttpContext context = new BasicHttpContext();
        context.setAttribute(Attributes.CORRELATOR, correlator);

        doThrow(new IOException()).when(correlator).write(any());

        assertThrows(UncheckedIOException.class, () ->
                unit.responseCompleted(context));
    }

}
