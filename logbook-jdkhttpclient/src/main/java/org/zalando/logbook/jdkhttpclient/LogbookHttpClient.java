package org.zalando.logbook.jdkhttpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LogbookHttpClient extends HttpClient {

    private static final Logger log = LoggerFactory.getLogger(LogbookHttpClient.class);

    private final HttpClient delegate;
    private final Logbook logbook;
    private final boolean decompressResponse;

    private LogbookHttpClient(final HttpClient delegate, final Logbook logbook, final boolean decompressResponse) {
        this.delegate = delegate;
        this.logbook = logbook;
        this.decompressResponse = decompressResponse;
    }

    public static HttpClient wrap(final HttpClient delegate, final Logbook logbook) {
        return wrap(delegate, logbook, false);
    }

    public static HttpClient wrap(final HttpClient delegate, final Logbook logbook, final boolean decompressResponse) {
        return new LogbookHttpClient(delegate, logbook, decompressResponse);
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

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public void shutdownNow() {
        delegate.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(final Duration duration) throws InterruptedException {
        return delegate.awaitTermination(duration);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public <T> HttpResponse<T> send(final HttpRequest request, final HttpResponse.BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException {
        if (bodyHandler == null) {
            return delegate.send(request, null);
        }
        final Exchange<T> exchange = exchange(request, bodyHandler);
        final HttpResponse<T> response = delegate.send(exchange.request, exchange.bodyHandler);
        exchange.log(response);
        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request,
            final HttpResponse.BodyHandler<T> bodyHandler) {
        if (bodyHandler == null) {
            return delegate.sendAsync(request, null);
        }
        final Exchange<T> exchange = exchange(request, bodyHandler);
        return delegate.sendAsync(exchange.request, exchange.bodyHandler).thenApply(response -> {
            exchange.log(response);
            return response;
        });
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request,
            final HttpResponse.BodyHandler<T> bodyHandler, final HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        if (bodyHandler == null || pushPromiseHandler == null) {
            return delegate.sendAsync(request, bodyHandler, pushPromiseHandler);
        }
        final Exchange<T> exchange = exchange(request, bodyHandler);
        return delegate.sendAsync(exchange.request, exchange.bodyHandler, (initiatingRequest, promisedRequest, acceptor) ->
                pushPromiseHandler.applyPushPromise(initiatingRequest, promisedRequest, pushedBodyHandler -> {
                    final Exchange<T> pushExchange = exchange(promisedRequest, pushedBodyHandler);
                    return acceptor.apply(pushExchange.bodyHandler).thenApply(response -> {
                        pushExchange.log(response);
                        return response;
                    });
                })).thenApply(response -> {
                    exchange.log(response);
                    return response;
                });
    }

    private <T> Exchange<T> exchange(final HttpRequest request, final HttpResponse.BodyHandler<T> bodyHandler) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder(request, (name, value) -> true);
        final BufferingBodyPublisher publisher = request.bodyPublisher().map(BufferingBodyPublisher::new).orElse(null);
        if (publisher != null) {
            builder.method(request.method(), publisher);
        }
        final HttpRequest copiedRequest = builder.build();
        final Exchange<T> exchange = new Exchange<>(copiedRequest, new BufferingBodyHandler<>(bodyHandler),
                requestStage(copiedRequest, publisher));
        if (publisher == null) {
            exchange.writeRequest();
        } else {
            publisher.onComplete(exchange::writeRequest);
            publisher.onIncomplete(exchange::skipRequest);
        }
        return exchange;
    }

    private RequestWritingStage requestStage(final HttpRequest request, final BufferingBodyPublisher publisher) {
        try {
            return logbook.process(new LocalRequest(request, publisher));
        } catch (final Exception e) {
            log.warn("Unable to log request. Will skip the request & response logging step.", e);
            return null;
        }
    }

    private final class Exchange<T> {
        private final HttpRequest request;
        private final BufferingBodyHandler<T> bodyHandler;
        private final RequestWritingStage stage;
        private final CompletableFuture<ResponseProcessingStage> responseStage = new CompletableFuture<>();
        private final AtomicBoolean requestWritten = new AtomicBoolean();

        private Exchange(final HttpRequest request, final BufferingBodyHandler<T> bodyHandler,
                final RequestWritingStage stage) {
            this.request = request;
            this.bodyHandler = bodyHandler;
            this.stage = stage;
        }

        private void writeRequest() {
            if (!requestWritten.compareAndSet(false, true)) {
                return;
            }
            if (stage == null) {
                responseStage.complete(null);
                return;
            }

            try {
                responseStage.complete(stage.write());
            } catch (final Exception e) {
                log.warn("Unable to log request. Will skip the request & response logging step.", e);
                responseStage.complete(null);
            }
        }

        private void skipRequest() {
            // An individual publisher subscription can be retried by the JDK.
        }

        private void log(final HttpResponse<T> response) {
            responseStage.thenCombine(bodyHandler.completion(), (stage, ignored) -> {
                if (stage == null) {
                    return null;
                }

                try {
                    stage.process(new RemoteResponse(response, bodyHandler, decompressResponse)).write();
                } catch (final Exception e) {
                    log.warn("Unable to log response. Will skip the response logging step.", e);
                }
                return null;
            });
        }
    }
}
