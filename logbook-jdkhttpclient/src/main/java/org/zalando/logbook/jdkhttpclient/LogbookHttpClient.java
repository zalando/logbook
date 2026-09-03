package org.zalando.logbook.jdkhttpclient;

import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;

import jakarta.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL)
@Slf4j
public final class LogbookHttpClient extends HttpClient {

    private final HttpClient delegate;
    private final Logbook logbook;

    public LogbookHttpClient(final Logbook logbook, final HttpClient delegate) {
        this.logbook = Objects.requireNonNull(logbook);
        this.delegate = Objects.requireNonNull(delegate);
    }

    public LogbookHttpClient(final HttpClient delegate, final Logbook logbook) {
        this(logbook, delegate);
    }

    public LogbookHttpClient(final Logbook logbook) {
        this(logbook, HttpClient.newHttpClient());
    }

    @Override
    public <T> HttpResponse<T> send(
            final HttpRequest request,
            final HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {

        final JdkRequest jdkRequest = new JdkRequest(request);
        final Logbook.ResponseProcessingStage stage = logRequest(jdkRequest);

        final ByteArrayOutputStream responseBodyCache = new ByteArrayOutputStream();
        final HttpResponse.BodyHandler<T> wrappedHandler = responseInfo -> {
            final HttpResponse.BodySubscriber<T> subscriber = responseBodyHandler.apply(responseInfo);
            return new TeeingBodySubscriber<>(subscriber, responseBodyCache);
        };

        final HttpResponse<T> response = delegate.send(jdkRequest.toHttpRequest(), wrappedHandler);

        logResponse(stage, response, responseBodyCache.toByteArray());

        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            final HttpRequest request,
            final HttpResponse.BodyHandler<T> responseBodyHandler) {
        throw new UnsupportedOperationException("sendAsync is not supported yet");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            final HttpRequest request,
            final HttpResponse.BodyHandler<T> responseBodyHandler,
            final HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException("sendAsync is not supported yet");
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return delegate.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return delegate.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return delegate.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return delegate.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return delegate.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return delegate.authenticator();
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public Optional<Executor> executor() {
        return delegate.executor();
    }

    @Override
    public WebSocket.Builder newWebSocketBuilder() {
        return delegate.newWebSocketBuilder();
    }

    @Nullable
    private Logbook.ResponseProcessingStage logRequest(final JdkRequest request) {
        Logbook.ResponseProcessingStage stage = null;
        try {
            stage = logbook.process(request).write();
        } catch (final Exception e) {
            log.warn("Unable to log request. Will skip the request & response logging step.", e);
        }
        return stage;
    }

    private static void logResponse(
            @Nullable final Logbook.ResponseProcessingStage stage,
            final HttpResponse<?> response,
            final byte[] responseBody) {
        if (stage != null) {
            try {
                stage.process(new RemoteResponse(response, responseBody)).write();
            } catch (final Exception e) {
                log.warn("Unable to log response. Will skip the response logging step.", e);
            }
        } else {
            log.warn("Unable to log response: ResponseProcessingStage is null. Will skip the response logging step.");
        }
    }
}
