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

    /**
     * Decorates the given Reactor Netty {@link HttpClient} with Logbook pipeline instrumentation.
     * Installs a {@link LogbookClientHandler} on HTTP/1.1 connections after the channel is configured
     * and on HTTP/2 streams once stream-specific handlers are available.
     *
     * @param httpClient the client to decorate with Logbook client-side logging
     * @param logbook the Logbook instance used by the installed {@link LogbookClientHandler}
     * @return a decorated {@link HttpClient} that adds Logbook client handlers for HTTP/1.1 and HTTP/2 traffic
     */
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

    /**
     * Decorates the given Reactor Netty {@link HttpServer} with Logbook pipeline instrumentation.
     * Installs a {@link LogbookServerHandler} after the server channel is configured so inbound
     * requests and outbound responses are logged for the connection.
     *
     * @param httpServer the server to decorate with Logbook server-side logging
     * @param logbook the Logbook instance used by the installed {@link LogbookServerHandler}
     * @return a decorated {@link HttpServer} that adds a Logbook server handler to configured channels
     */
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
