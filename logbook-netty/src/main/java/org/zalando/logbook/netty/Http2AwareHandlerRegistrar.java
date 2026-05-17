package org.zalando.logbook.netty;

import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.apiguardian.api.API;
import org.zalando.logbook.Logbook;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.http.server.HttpServer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Installs {@link LogbookClientHandler} and {@link LogbookServerHandler} on Reactor Netty
 * connections, supporting both HTTP/1.1 and HTTP/2 (including H2C).
 *
 * <p>Usage:
 * <pre>{@code
 * HttpClient client = Http2AwareHandlerRegistrar.installOnClient(HttpClient.create(), logbook);
 * HttpServer server = Http2AwareHandlerRegistrar.installOnServer(HttpServer.create(), logbook);
 * }</pre>
 */
@API(status = EXPERIMENTAL)
public final class Http2AwareHandlerRegistrar {

    private Http2AwareHandlerRegistrar() {}

    public static HttpClient installOnClient(final HttpClient httpClient, final Logbook logbook) {
        return httpClient.observe((connection, state) -> {
            if (state == HttpClientState.STREAM_CONFIGURED) {
                connection.addHandlerLast(new LogbookClientHandler(logbook));
            } else if (state == ConnectionObserver.State.CONFIGURED) {
                final var pipeline = connection.channel().pipeline();
                if (pipeline.get(Http2ConnectionHandler.class) == null
                        && pipeline.get(LogbookClientHandler.class) == null) {
                    connection.addHandlerLast(new LogbookClientHandler(logbook));
                }
            }
        });
    }

    public static HttpServer installOnServer(final HttpServer httpServer, final Logbook logbook) {
        return httpServer.observe((connection, state) -> {
            if (state == ConnectionObserver.State.CONFIGURED) {
                final var pipeline = connection.channel().pipeline();
                if (pipeline.get(LogbookServerHandler.class) == null) {
                    connection.addHandlerLast(new LogbookServerHandler(logbook));
                }
            }
        });
    }
}
