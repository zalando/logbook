package org.zalando.logbook.netty;

import io.netty.channel.ChannelPipeline;
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
 * <p>This registrar is intended for Reactor Netty applications, including those outside Spring.
 * Direct users need compatible {@code reactor-netty-core} and {@code reactor-netty-http}
 * dependencies. HTTP/2 also requires a compatible {@code netty-codec-http2} dependency; it is
 * not required for HTTP/1.1-only usage.
 *
 * <p>Do not install the handlers with raw {@code pipeline().addLast()} for this HTTP/2 integration.
 * Use this registrar, which adds handlers at the appropriate connection lifecycle state.
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
                if (!Http2.hasConnectionHandler(pipeline)
                        && pipeline.get(LogbookClientHandler.class) == null) {
                    connection.addHandlerLast(new LogbookClientHandler(logbook));
                }
            }
        });
    }

    /**
     * Decorates the given Reactor Netty {@link HttpServer} with Logbook pipeline instrumentation.
     * Installs a {@link LogbookServerHandler} after the server channel is configured so inbound
     * requests and outbound responses are logged for the connection. {@link
     * ConnectionObserver.State#CONFIGURED CONFIGURED} is generic and covers HTTP/1.1 and HTTP/2
     * stream setup; the per-channel guard prevents duplicate installation when it repeats for a
     * keep-alive connection.
     *
     * @param httpServer the server to decorate with Logbook server-side logging
     * @param logbook the Logbook instance used by the installed {@link LogbookServerHandler}
     * @return a decorated {@link HttpServer} that adds a Logbook server handler to configured channels
     */
    public static HttpServer installOnServer(final HttpServer httpServer, final Logbook logbook) {
        return httpServer.childObserve((connection, state) -> {
            if (state == ConnectionObserver.State.CONFIGURED) {
                final var pipeline = connection.channel().pipeline();
                if (pipeline.get(LogbookServerHandler.class) == null) {
                    connection.addHandlerLast(new LogbookServerHandler(logbook));
                }
            }
        });
    }

    private static final class Http2 {

        private Http2() {
        }

        private static boolean hasConnectionHandler(final ChannelPipeline pipeline) {
            for (final var handler : pipeline.toMap().values()) {
                for (Class<?> type = handler.getClass(); type != null; type = type.getSuperclass()) {
                    if (type.getName().equals("io.netty.handler.codec.http2.Http2ConnectionHandler")) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
