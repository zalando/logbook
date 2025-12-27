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
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.create;
import static org.apache.http.nio.client.methods.HttpAsyncMethods.createConsumer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class LogbookHttpAsyncResponseConsumerTest extends AbstractHttpTest {

    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .build();

    @SuppressWarnings("unchecked")
    private final FutureCallback<HttpResponse> callback = mock(FutureCallback.class);
    private final ResponseProcessingStage stage = mock(ResponseProcessingStage.class);

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
        final HttpUriRequest request;

        if (body == null) {
            request = new HttpGet(server.baseUrl());
        } else {
            final HttpPost post = new HttpPost(server.baseUrl());
            post.setEntity(new StringEntity(body));
            post.setHeader(CONTENT_TYPE, TEXT_PLAIN.toString());
            request = post;
        }

        return client.execute(create(request),
                new LogbookHttpAsyncResponseConsumer<>(createConsumer(), false), callback).get();
    }

    @Test
    void shouldNotPropagateException() throws IOException {
        final HttpAsyncResponseConsumer<HttpResponse> unit = new LogbookHttpAsyncResponseConsumer<>(createConsumer(), false);

        final BasicHttpContext context = new BasicHttpContext();
        context.setAttribute(Attributes.STAGE, stage);

        final ResponseWritingStage last = mock(ResponseWritingStage.class);

        when(stage.process(any())).thenReturn(last);

        doThrow(new IOException()).when(last).write();

        unit.responseCompleted(context);

        verify(last).write();
    }

}
