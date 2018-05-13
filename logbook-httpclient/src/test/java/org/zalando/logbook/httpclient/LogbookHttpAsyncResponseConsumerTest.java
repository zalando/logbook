package org.zalando.logbook.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Correlator;
import org.zalando.logbook.Logbook;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.create;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.createConsumer;
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
    protected HttpResponse sendAndReceive(@Nullable final String body) throws IOException, ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/"),
                giveResponse("Hello, world!", "text/plain"));

        final HttpUriRequest request;

        if (body == null) {
            request = new HttpGet(driver.getBaseUrl());
        } else {
            final HttpPost post = new HttpPost(driver.getBaseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());
            request = post;
        }

        return client.execute(create(request),
                new LogbookHttpAsyncResponseConsumer<>(createConsumer()), callback).get();
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
