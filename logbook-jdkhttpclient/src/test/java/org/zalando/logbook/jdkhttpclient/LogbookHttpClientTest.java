package org.zalando.logbook.jdkhttpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

final class LogbookHttpClientTest {

    private final CapturingWriter writer = new CapturingWriter();
    private final WireMockServer server = new WireMockServer(options().dynamicPort().gzipDisabled(true));

    @BeforeEach
    void startServer() {
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void logsSyncExchangeWithoutChangingStringBody() throws Exception {
        server.stubFor(post("/").willReturn(aResponse().withBody("response")));

        final HttpResponse<String> response = client().send(postRequest(), ofString());

        assertThat(response.body()).isEqualTo("response");
        server.verify(postRequestedFor(com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/"))
                .withRequestBody(equalTo("request")));
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("request"))
                .anySatisfy(message -> assertThat(message).contains("response"));
    }

    @Test
    void logsAsyncExchangeWithoutChangingStringBody() {
        server.stubFor(post("/").willReturn(aResponse().withBody("response")));

        final HttpResponse<String> response = client().sendAsync(postRequest(), ofString()).join();

        assertThat(response.body()).isEqualTo("response");
        server.verify(postRequestedFor(com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/"))
                .withRequestBody(equalTo("request")));
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("request"))
                .anySatisfy(message -> assertThat(message).contains("response"));
    }

    @Test
    void logsInputStreamResponseOnlyAfterTheStreamCompletes() throws Exception {
        server.stubFor(get("/").willReturn(aResponse().withBody("streamed-response").withChunkedDribbleDelay(2, 1_000)));

        final HttpResponse<InputStream> response = client().send(getRequest(), ofInputStream());

        assertThat(response.body()).isNotNull();
        assertThat(writer.responseMessage()).isNotDone();
        try (InputStream body = response.body()) {
            assertThat(body.readAllBytes()).isEqualTo("streamed-response".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        assertThat(writer.responseMessage().join()).contains("streamed-response");
    }

    @Test
    void doesNotLogResponseWhenBodyDeliveryFails() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            final HttpResponse.BodySubscriber<String> subscriber = invocation.<HttpResponse.BodyHandler<String>>getArgument(1)
                    .apply(new ResponseInfo());
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(final long count) {
                }

                @Override
                public void cancel() {
                }
            });
            subscriber.onError(new IOException("body failed"));
            return response(invocation.getArgument(0), "response");
        });

        final HttpResponse<String> response = LogbookHttpClient.wrap(delegate, logbook(writer)).send(getRequest(), ofString());

        assertThat(response.body()).isEqualTo("response");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void preservesCustomResponseBodyHandlerResult() throws Exception {
        server.stubFor(get("/").willReturn(aResponse().withBody("response")));
        final HttpResponse.BodyHandler<Integer> handler = info -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(java.nio.charset.StandardCharsets.UTF_8), ignored -> 42);

        final HttpResponse<Integer> response = client().send(getRequest(), handler);

        assertThat(response.body()).isEqualTo(42);
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("response"));
    }

    @Test
    void logsHttpErrorResponse() throws Exception {
        server.stubFor(get("/").willReturn(aResponse().withStatus(500).withBody("response")));

        final HttpResponse<String> response = client().send(getRequest(), ofString());

        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("500"))
                .anySatisfy(message -> assertThat(message).contains("response"));
    }

    @Test
    void doesNotChangeResponseWhenWriterThrows() throws Exception {
        final HttpLogWriter failingWriter = mock(HttpLogWriter.class);
        doThrow(new IOException("writer failed")).when(failingWriter).write(any(Precorrelation.class), any());
        final Logbook logbook = logbook(failingWriter);
        server.stubFor(get("/").willReturn(aResponse().withBody("response")));

        final HttpResponse<String> response = LogbookHttpClient.wrap(HttpClient.newHttpClient(), logbook)
                .send(getRequest(), ofString());

        assertThat(response.body()).isEqualTo("response");
    }

    @Test
    void doesNotChangeResponseWhenResponseWriterThrows() throws Exception {
        final HttpLogWriter failingWriter = mock(HttpLogWriter.class);
        doThrow(new IOException("writer failed")).when(failingWriter).write(any(Correlation.class), any());
        final Logbook logbook = logbook(failingWriter);
        server.stubFor(get("/").willReturn(aResponse().withBody("response")));

        final HttpResponse<String> response = LogbookHttpClient.wrap(HttpClient.newHttpClient(), logbook)
                .send(getRequest(), ofString());

        assertThat(response.body()).isEqualTo("response");
    }

    @Test
    void propagatesDelegateFailureWithoutLoggingResponse() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.send(any(), any())).thenThrow(new IOException("transport failed"));

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer)).send(getRequest(), ofString()))
                .isInstanceOf(IOException.class)
                .hasMessage("transport failed");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void propagatesAsyncDelegateFailureWithoutLoggingResponse() {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.sendAsync(any(), any())).thenReturn(CompletableFuture.failedFuture(
                new IllegalStateException("transport failed")));

        final CompletableFuture<HttpResponse<String>> future = LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(getRequest(), ofString());

        assertThatThrownBy(future::join).hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("transport failed");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void writesCompletePublisherBodyOnceBeforePropagatingTransportFailure() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            assertThat(writer.requests).isEmpty();
            publisher.complete();
            assertThat(writer.requests).hasSize(1);
            assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("request"));
            publisher.complete();
            assertThat(writer.requests).hasSize(1);
            throw new IOException("transport failed");
        });

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString())).isInstanceOf(IOException.class);

        assertThat(writer.requests).hasSize(1);
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void writesOneCompleteRequestWhenPublisherIsSubscribedTwice() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            subscribe(invocation.getArgument(0), false);
            publisher.complete();
            return response(invocation.getArgument(0), "response");
        });

        final HttpResponse<String> response = LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString());

        assertThat(response.body()).isEqualTo("response");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.messages().stream().filter(message -> message.contains("request"))).hasSize(1);
    }

    @Test
    void retainsCompletedRequestWhenLaterSubscriptionIsCancelled() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            publisher.complete();
            subscribe(invocation.getArgument(0), true);
            return response(invocation.getArgument(0), "response");
        });

        final HttpResponse<String> response = LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString());

        assertThat(response.body()).isEqualTo("response");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("request"));
    }

    @Test
    void writesOneCompleteRequestWhenSubscriptionIsCancelledBeforeAnotherCompletes() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), true);
            subscribe(invocation.getArgument(0), false);
            publisher.complete(1);
            publisher.complete(1);
            return response(invocation.getArgument(0), "response");
        });

        final HttpResponse<String> response = LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString());

        assertThat(response.body()).isEqualTo("response");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.messages().stream().filter(message -> message.contains("request"))).hasSize(1);
    }

    @Test
    void writesOneCompleteRequestWhenSubscriptionFailsBeforeAnotherCompletes() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            subscribe(invocation.getArgument(0), false);
            publisher.fail(0);
            publisher.complete(1);
            publisher.complete(1);
            return response(invocation.getArgument(0), "response");
        });

        final HttpResponse<String> response = LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString());

        assertThat(response.body()).isEqualTo("response");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.messages().stream().filter(message -> message.contains("request"))).hasSize(1);
    }

    @Test
    void writesCompletePublisherBodyBeforeAsyncTransportFailure() {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>sendAsync(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            publisher.complete();
            return CompletableFuture.failedFuture(new IllegalStateException("transport failed"));
        });

        final CompletableFuture<HttpResponse<String>> future = LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(request(publisher), ofString());

        assertThatThrownBy(future::join).hasRootCauseMessage("transport failed");
        assertThat(writer.requests).hasSize(1);
        assertThat(writer.messages()).anySatisfy(message -> assertThat(message).contains("request"));
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void forwardsNullBodyHandlerToDelegate() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), org.mockito.ArgumentMatchers.isNull())).thenThrow(new NullPointerException());

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer)).send(getRequest(), null))
                .isInstanceOf(NullPointerException.class);
        verify(delegate).send(getRequest(), null);
    }

    @Test
    void returnsDelegateResponseForNullBodyHandler() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        final HttpRequest request = getRequest();
        final HttpResponse<String> expected = response(request, "response");
        when(delegate.<String>send(request, null)).thenReturn(expected);

        final HttpResponse<String> actual = LogbookHttpClient.wrap(delegate, logbook(writer)).send(request, null);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void forwardsNullAsyncHandlersToDelegate() {
        final HttpClient delegate = mock(HttpClient.class);
        final HttpRequest request = getRequest();
        when(delegate.sendAsync(any(), org.mockito.ArgumentMatchers.isNull())).thenReturn(CompletableFuture.failedFuture(
                new NullPointerException()));
        when(delegate.sendAsync(any(), any(), org.mockito.ArgumentMatchers.isNull())).thenReturn(
                CompletableFuture.failedFuture(new NullPointerException()));
        when(delegate.sendAsync(any(), org.mockito.ArgumentMatchers.isNull(), any())).thenReturn(
                CompletableFuture.failedFuture(new NullPointerException()));
        final HttpResponse.PushPromiseHandler<String> pushPromiseHandler =
                (initiating, promised, acceptor) -> acceptor.apply(ofString());

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer)).sendAsync(request, null).join())
                .hasRootCauseInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(request, ofString(), null).join()).hasRootCauseInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(request, (HttpResponse.BodyHandler<String>) null, pushPromiseHandler).join())
                .hasRootCauseInstanceOf(NullPointerException.class);

        verify(delegate).sendAsync(request, null);
        verify(delegate).sendAsync(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull());
        verify(delegate).sendAsync(org.mockito.ArgumentMatchers.eq(request), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void writesNoBodyRequestBeforeDelegating() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            assertThat(writer.requests).hasSize(1);
            return response(invocation.getArgument(0), "response");
        });

        LogbookHttpClient.wrap(delegate, logbook(writer)).send(getRequest(), ofString());

    }

    @Test
    void doesNotWriteRequestWhenPublisherFails() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            publisher.fail();
            throw new IOException("transport failed");
        });

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString())).isInstanceOf(IOException.class);

        assertThat(writer.requests).isEmpty();
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void doesNotWriteRequestWhenPublisherIsCancelled() throws Exception {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), true);
            publisher.fail();
            throw new IOException("transport failed");
        });

        assertThatThrownBy(() -> LogbookHttpClient.wrap(delegate, logbook(writer))
                .send(request(publisher), ofString())).isInstanceOf(IOException.class);

        assertThat(writer.requests).isEmpty();
        assertThat(writer.responses).isEmpty();
    }

    @Test
    void returnsAsyncResponseBeforeRequestPublisherCompletes() {
        final ControlledPublisher publisher = new ControlledPublisher("request");
        final HttpClient delegate = mock(HttpClient.class);
        final CompletableFuture<HttpResponse<String>> delegateResponse = new CompletableFuture<>();
        when(delegate.<String>sendAsync(any(), any())).thenAnswer(invocation -> {
            subscribe(invocation.getArgument(0), false);
            return delegateResponse;
        });

        final CompletableFuture<HttpResponse<String>> response = LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(request(publisher), ofString());
        assertThat(writer.requests).isEmpty();
        delegateResponse.complete(response(getRequest(), "response"));
        assertThat(response.join().body()).isEqualTo("response");
        assertThat(writer.events).isEmpty();
        publisher.complete();

        assertThat(writer.events).containsExactly("request");
    }

    @Test
    void doesNotChangeResponseWhenRequestProcessingFails() throws Exception {
        final Logbook logbook = mock(Logbook.class);
        final HttpClient delegate = mock(HttpClient.class);
        final HttpResponse<String> expected = response(getRequest(), "response");
        when(logbook.process(any())).thenThrow(new IOException("logging failed"));
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            complete(invocation.getArgument(1));
            return expected;
        });

        final HttpResponse<String> actual = LogbookHttpClient.wrap(delegate, logbook).send(getRequest(), ofString());

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void doesNotChangeResponseWhenRequestWritingFails() throws Exception {
        final Logbook logbook = mock(Logbook.class);
        final Logbook.RequestWritingStage requestStage = mock(Logbook.RequestWritingStage.class);
        final HttpClient delegate = mock(HttpClient.class);
        final HttpResponse<String> expected = response(getRequest(), "response");
        when(logbook.process(any())).thenReturn(requestStage);
        when(requestStage.write()).thenThrow(new IOException("logging failed"));
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            complete(invocation.getArgument(1));
            return expected;
        });

        final HttpResponse<String> actual = LogbookHttpClient.wrap(delegate, logbook).send(getRequest(), ofString());

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void doesNotChangeResponseWhenResponseProcessingFails() throws Exception {
        final Logbook logbook = mock(Logbook.class);
        final Logbook.RequestWritingStage requestStage = mock(Logbook.RequestWritingStage.class);
        final Logbook.ResponseProcessingStage responseStage = mock(Logbook.ResponseProcessingStage.class);
        final Logbook.ResponseWritingStage responseWriter = mock(Logbook.ResponseWritingStage.class);
        final HttpClient delegate = mock(HttpClient.class);
        final HttpResponse<String> expected = response(getRequest(), "response");
        when(logbook.process(any())).thenReturn(requestStage);
        when(requestStage.write()).thenReturn(responseStage);
        when(responseStage.process(any())).thenReturn(responseWriter);
        doThrow(new IOException("logging failed")).when(responseWriter).write();
        when(delegate.<String>send(any(), any())).thenAnswer(invocation -> {
            complete(invocation.getArgument(1));
            return expected;
        });

        final HttpResponse<String> actual = LogbookHttpClient.wrap(delegate, logbook).send(getRequest(), ofString());

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void forwardsDelegateConfigurationMethods() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        final WebSocket.Builder webSocketBuilder = mock(WebSocket.Builder.class);
        final SSLContext sslContext = SSLContext.getDefault();
        final SSLParameters sslParameters = new SSLParameters();
        when(delegate.cookieHandler()).thenReturn(Optional.of(mock(CookieHandler.class)));
        when(delegate.connectTimeout()).thenReturn(Optional.of(Duration.ofSeconds(1)));
        when(delegate.followRedirects()).thenReturn(HttpClient.Redirect.ALWAYS);
        when(delegate.proxy()).thenReturn(Optional.of(mock(ProxySelector.class)));
        when(delegate.sslContext()).thenReturn(sslContext);
        when(delegate.sslParameters()).thenReturn(sslParameters);
        when(delegate.authenticator()).thenReturn(Optional.of(mock(Authenticator.class)));
        when(delegate.version()).thenReturn(HttpClient.Version.HTTP_2);
        when(delegate.executor()).thenReturn(Optional.of(mock(Executor.class)));
        when(delegate.newWebSocketBuilder()).thenReturn(webSocketBuilder);
        final HttpClient client = LogbookHttpClient.wrap(delegate, logbook(writer));

        assertThat(client.cookieHandler()).isPresent();
        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(1));
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.ALWAYS);
        assertThat(client.proxy()).isPresent();
        assertThat(client.sslContext()).isSameAs(sslContext);
        assertThat(client.sslParameters()).isSameAs(sslParameters);
        assertThat(client.authenticator()).isPresent();
        assertThat(client.version()).isEqualTo(HttpClient.Version.HTTP_2);
        assertThat(client.executor()).isPresent();
        assertThat(client.newWebSocketBuilder()).isSameAs(webSocketBuilder);
    }

    @Test
    void forwardsDelegateLifecycleMethods() throws Exception {
        final HttpClient delegate = mock(HttpClient.class);
        when(delegate.awaitTermination(Duration.ofSeconds(1))).thenReturn(true);
        when(delegate.isTerminated()).thenReturn(true);
        final HttpClient client = LogbookHttpClient.wrap(delegate, logbook(writer));

        client.shutdown();
        client.shutdownNow();
        assertThat(client.isTerminated()).isTrue();
        assertThat(client.awaitTermination(Duration.ofSeconds(1))).isTrue();
        client.close();

        verify(delegate).shutdown();
        verify(delegate).shutdownNow();
        verify(delegate).isTerminated();
        verify(delegate).awaitTermination(Duration.ofSeconds(1));
        verify(delegate).close();
    }

    @Test
    void wrapsPushPromiseHandlerAndLogsAnIndependentExchange() {
        final HttpClient delegate = mock(HttpClient.class);
        final HttpRequest initialRequest = getRequest();
        final HttpRequest promisedRequest = HttpRequest.newBuilder(serverUri("/push")).GET().build();
        final HttpResponse<String> initialResponse = response(initialRequest, "initial");
        final HttpResponse<String> promisedResponse = response(promisedRequest, "pushed");
        final CompletableFuture<HttpResponse<String>> initialFuture = new CompletableFuture<>();
        final CompletableFuture<HttpResponse<String>> promisedFuture = new CompletableFuture<>();
        final ArgumentCaptor<HttpResponse.BodyHandler<String>> initialBodyHandler = ArgumentCaptor.forClass(HttpResponse.BodyHandler.class);
        final ArgumentCaptor<HttpResponse.PushPromiseHandler<String>> pushHandler = ArgumentCaptor.forClass(HttpResponse.PushPromiseHandler.class);
        when(delegate.sendAsync(any(), initialBodyHandler.capture(), pushHandler.capture())).thenReturn(initialFuture);

        final CompletableFuture<HttpResponse<String>> result = LogbookHttpClient.wrap(delegate, logbook(writer))
                .sendAsync(initialRequest, ofString(), (initiating, promised, acceptor) -> acceptor.apply(ofString()));
        final Function<HttpResponse.BodyHandler<String>, CompletableFuture<HttpResponse<String>>> acceptor = handler -> {
            assertThat(handler).isInstanceOf(BufferingBodyHandler.class);
            complete(handler);
            return promisedFuture;
        };
        pushHandler.getValue().applyPushPromise(initialRequest, promisedRequest, acceptor);
        promisedFuture.complete(promisedResponse);
        complete(initialBodyHandler.getValue());
        initialFuture.complete(initialResponse);

        assertThat(result.join()).isSameAs(initialResponse);
        assertThat(writer.requests).hasSize(2);
        assertThat(writer.responses).hasSize(2);
        assertThat(writer.requests.stream().map(Precorrelation::getId)).doesNotHaveDuplicates();
    }

    private HttpClient client() {
        return LogbookHttpClient.wrap(HttpClient.newHttpClient(), logbook(writer));
    }

    private Logbook logbook(final HttpLogWriter logWriter) {
        return Logbook.builder().strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), logWriter)).build();
    }

    private HttpRequest postRequest() {
        return HttpRequest.newBuilder(serverUri("/")).header("X-Test", "value").POST(ofString("request")).build();
    }

    private HttpRequest getRequest() {
        return HttpRequest.newBuilder(serverUri("/")).GET().build();
    }

    private HttpRequest request(final HttpRequest.BodyPublisher publisher) {
        return HttpRequest.newBuilder(serverUri("/")).POST(publisher).build();
    }

    private static void subscribe(final HttpRequest request, final boolean cancel) {
        request.bodyPublisher().orElseThrow().subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(final Subscription subscription) {
                if (cancel) {
                    subscription.cancel();
                } else {
                    subscription.request(1);
                }
            }

            @Override
            public void onNext(final ByteBuffer item) {
            }

            @Override
            public void onError(final Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private URI serverUri(final String path) {
        return URI.create(server.baseUrl() + path);
    }

    private static HttpResponse<String> response(final HttpRequest request, final String body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return request;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<javax.net.ssl.SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return request.uri();
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }

    private static void complete(final HttpResponse.BodyHandler<String> handler) {
        final HttpResponse.BodySubscriber<String> subscriber = handler.apply(new ResponseInfo());
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(final long count) {
            }

            @Override
            public void cancel() {
            }
        });
        subscriber.onComplete();
    }

    private static final class ResponseInfo implements HttpResponse.ResponseInfo {
        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    private static final class CapturingWriter implements HttpLogWriter {
        private final List<Precorrelation> requests = new ArrayList<>();
        private final List<Correlation> responses = new ArrayList<>();
        private final List<String> messages = new ArrayList<>();
        private final List<String> events = new ArrayList<>();
        private final CompletableFuture<String> responseMessage = new CompletableFuture<>();

        @Override
        public void write(final Precorrelation precorrelation, final String request) {
            requests.add(precorrelation);
            messages.add(request);
            events.add("request");
        }

        @Override
        public void write(final Correlation correlation, final String response) {
            responses.add(correlation);
            messages.add(response);
            events.add("response");
            responseMessage.complete(response);
        }

        List<String> messages() {
            return messages;
        }

        CompletableFuture<String> responseMessage() {
            return responseMessage;
        }
    }

    private static final class ControlledPublisher implements HttpRequest.BodyPublisher {
        private final byte[] body;
        private final List<Subscriber<? super ByteBuffer>> subscribers = new ArrayList<>();

        private ControlledPublisher(final String body) {
            this.body = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public long contentLength() {
            return body.length;
        }

        @Override
        public void subscribe(final Subscriber<? super ByteBuffer> subscriber) {
            subscribers.add(subscriber);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(final long count) {
                }

                @Override
                public void cancel() {
                }
            });
        }

        private void complete() {
            subscribers.forEach(subscriber -> {
                subscriber.onNext(ByteBuffer.wrap(body));
                subscriber.onComplete();
            });
        }

        private void complete(final int index) {
            final Subscriber<? super ByteBuffer> subscriber = subscribers.get(index);
            subscriber.onNext(ByteBuffer.wrap(body));
            subscriber.onComplete();
        }

        private void fail() {
            subscribers.forEach(subscriber -> subscriber.onError(new IllegalStateException("publisher failed")));
        }

        private void fail(final int index) {
            subscribers.get(index).onError(new IllegalStateException("publisher failed"));
        }
    }
}
