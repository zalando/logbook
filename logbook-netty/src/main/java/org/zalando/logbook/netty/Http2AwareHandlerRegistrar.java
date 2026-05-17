package org.zalando.logbook.netty;

import io.netty.handler.codec.http2.Http2ConnectionHandler;
import lombok.RequiredArgsConstructor;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.http.server.HttpServer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Installs {@link LogbookClientHandler} and {@link LogbookServerHandler} on Reactor Netty
 * connections, including HTTP/2 stream channels.
 *
 * <p>For HTTP/2 clients, uses {@link HttpClientState#STREAM_CONFIGURED} to install the handler
 * on each virtual stream channel rather than the parent TCP connection. For HTTP/1.1 clients,
 * {@link ConnectionObserver.State#CONFIGURED} is used when no {@link Http2ConnectionHandler}
 * is detected in the pipeline.
 *
 * <p>Usage:
 * <pre>{@code
 * Http2AwareHandlerRegistrar registrar = new Http2AwareHandlerRegistrar(logbook);
 * HttpClient client = HttpClient.create()
 *     .observe(registrar.clientObserver());
 * HttpServer server = HttpServer.create()
 *     .childObserve(registrar.serverObserver());
 * }</pre>
 */
@API(status = EXPERIMENTAL)
@RequiredArgsConstructor
public final class Http2AwareHandlerRegistrar {

    private final Logbook logbook;

    public ConnectionObserver clientObserver() {
        return (connection, state) -> {
            if (state == HttpClientState.STREAM_CONFIGURED) {
                // H2 stream channel: install directly (stream channel is per-request)
                connection.addHandlerLast(new LogbookClientHandler(logbook));
            } else if (state == ConnectionObserver.State.CONFIGURED) {
                // HTTP/1.1 connection: guard against parent H2 TCP channel and duplicate installs
                final var pipeline = connection.channel().pipeline();
                if (pipeline.get(Http2ConnectionHandler.class) == null
                        && pipeline.get(LogbookClientHandler.class) == null) {
                    connection.addHandlerLast(new LogbookClientHandler(logbook));
                }
            }
        };
    }

    public ConnectionObserver serverObserver() {
        return (connection, state) -> {
            if (state == ConnectionObserver.State.CONFIGURED) {
                final var pipeline = connection.channel().pipeline();
                if (pipeline.get(LogbookServerHandler.class) == null) {
                    connection.addHandlerLast(new LogbookServerHandler(logbook));
                }
            }
        };
    }
}
