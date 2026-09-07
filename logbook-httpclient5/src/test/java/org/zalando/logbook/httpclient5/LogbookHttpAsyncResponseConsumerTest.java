package org.zalando.logbook.httpclient5;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityProducer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;

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
    protected ClassicHttpResponse sendAndReceive(final WireMockServer server, @Nullable final String body) throws ExecutionException, InterruptedException {
        SimpleRequestBuilder builder;
        if (body == null) {
            builder = SimpleRequestBuilder.get(server.baseUrl());
        } else {
            builder = SimpleRequestBuilder.post(server.baseUrl()).setBody(body, TEXT_PLAIN).setHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN.toString());
        }

        AtomicReference<String> responseRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        HttpResponse response = client.execute(SimpleRequestProducer.create(builder.build()), new LogbookHttpAsyncResponseConsumer<>(SimpleResponseConsumer.create(), SimpleHttpResponse::getBodyBytes, true), HttpClientContext.create(), getCallback(responseRef, latch)).get();

        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(response.getCode(), response.getReasonPhrase());
        latch.await(5, SECONDS);
        String responseBody = responseRef.get();
        if (responseBody != null) httpResponse.setEntity(new StringEntity(responseBody));
        return httpResponse;
    }

    private ClassicHttpResponse sendAndReceiveWithChunkedBody(WireMockServer server, final String body)
            throws ExecutionException, InterruptedException {
        BasicHttpRequest request = new BasicHttpRequest(Method.POST, URI.create(server.baseUrl()));
        request.setHeader(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN.toString());

        AtomicReference<String> responseRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        HttpResponse response = client.execute(
                new BasicRequestProducer(request, new StringAsyncEntityProducer(body, TEXT_PLAIN)),
                new LogbookHttpAsyncResponseConsumer<>(SimpleResponseConsumer.create(), SimpleHttpResponse::getBodyBytes, true),
                HttpClientContext.create(),
                getCallback(responseRef, latch)
        ).get();

        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(response.getCode(), response.getReasonPhrase());
        latch.await(5, SECONDS);
        String responseBody = responseRef.get();
        if (responseBody != null) httpResponse.setEntity(new StringEntity(responseBody));
        return httpResponse;
    }

    @Test
    void shouldLogRequestWithBodyOfUnknownContentLength() throws IOException, ExecutionException, InterruptedException {
        server.stubFor(post("/").withRequestBody(equalTo("Hello, world!")).willReturn(aResponse().withStatus(204)));

        sendAndReceiveWithChunkedBody(server, "Hello, world!");

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(
                        format("POST http://localhost:%d/ HTTP/1.1", server.port()),
                        "Content-Type: text/plain",
                        "Hello, world!");
    }

    private static FutureCallback<SimpleHttpResponse> getCallback(AtomicReference<String> responseRef, CountDownLatch latch) {
        return new FutureCallback<SimpleHttpResponse>() {
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
        };
    }
}
