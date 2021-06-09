package org.zalando.logbook.httpclient5;

import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;

public final class LogbookHttpAsyncResponseConsumerTest extends AbstractHttpTest {

    private final CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create()
            .addRequestInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
            .build();

    @BeforeEach
    void start() {
        client.start();
    }

    @AfterEach
    void stop() throws IOException {
        client.close();
    }

    @Override
    protected ClassicHttpResponse sendAndReceive(@Nullable final String body) throws ExecutionException, InterruptedException {
        driver.addExpectation(onRequestTo("/"), giveResponse("Hello, world!", "text/plain"));

        SimpleRequestBuilder builder;
        if (body == null) {
            builder = SimpleRequestBuilder.get(driver.getBaseUrl());
        } else {
            builder = SimpleRequestBuilder.post(driver.getBaseUrl()).setBody(body, TEXT_PLAIN).setHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN.toString());
        }

        AtomicReference<String> responseRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        HttpResponse response = client.execute(SimpleRequestProducer.create(builder.build()), new LogbookHttpAsyncResponseConsumer<>(SimpleResponseConsumer.create()), new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                responseRef.set(result.getBodyText());
                latch.countDown();
            }

            @Override
            public void failed(Exception ex) {
                latch.countDown();
            }

            @Override
            public void cancelled() {
                latch.countDown();
            }
        }).get();

        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(response.getCode(), response.getReasonPhrase());
        latch.await(5, SECONDS);
        String responseBody = responseRef.get();
        if (responseBody != null) httpResponse.setEntity(new StringEntity(responseBody));
        return httpResponse;
    }
}