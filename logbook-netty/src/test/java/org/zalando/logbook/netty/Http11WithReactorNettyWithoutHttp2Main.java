package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.zalando.logbook.Logbook;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.util.Map;

import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public final class Http11WithReactorNettyWithoutHttp2Main {

    private Http11WithReactorNettyWithoutHttp2Main() {
    }

    public static void main(final String[] args) {
        assertUnreachable("io.netty.handler.codec.http2.Http2ConnectionHandler");

        final ConnectionObserver clientObserver = installClient(mock(Logbook.class));
        final Connection clientConnection = connectionWithoutHandlers();
        clientObserver.onStateChange(clientConnection, ConnectionObserver.State.CONFIGURED);
        verify(clientConnection).addHandlerLast(any(LogbookClientHandler.class));

        final ConnectionObserver serverObserver = installServer(mock(Logbook.class));
        final Connection serverConnection = connectionWithoutHandlers();
        serverObserver.onStateChange(serverConnection, ConnectionObserver.State.CONFIGURED);
        verify(serverConnection).addHandlerLast(any(LogbookServerHandler.class));

        System.out.println("HTTP11_WITHOUT_HTTP2_SUCCESS");
    }

    private static ConnectionObserver installClient(final Logbook logbook) {
        final HttpClient client = mock(HttpClient.class);
        final ArgumentCaptor<ConnectionObserver> observer = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(client.observe(observer.capture())).thenReturn(mock(HttpClient.class));
        Http2AwareHandlerRegistrar.installOnClient(client, logbook);
        return observer.getValue();
    }

    private static ConnectionObserver installServer(final Logbook logbook) {
        final HttpServer server = mock(HttpServer.class);
        final ArgumentCaptor<ConnectionObserver> observer = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(server.childObserve(observer.capture())).thenReturn(mock(HttpServer.class));
        Http2AwareHandlerRegistrar.installOnServer(server, logbook);
        return observer.getValue();
    }

    private static Connection connectionWithoutHandlers() {
        final Connection connection = mock(Connection.class);
        final Channel channel = mock(Channel.class);
        final ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(connection.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        when(pipeline.toMap()).thenReturn(Map.of());
        when(pipeline.get(LogbookClientHandler.class)).thenReturn(null);
        when(pipeline.get(LogbookServerHandler.class)).thenReturn(null);
        return connection;
    }

    private static void assertUnreachable(final String className) {
        try {
            Class.forName(className);
            throw new AssertionError(className + " must be unreachable");
        } catch (final ClassNotFoundException ignored) {
            // Expected: this subprocess models a Reactor Netty HTTP/1.1-only consumer.
        }
    }
}
