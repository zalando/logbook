package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.Logbook;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClientState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Http2AwareHandlerRegistrarTest {

    private final Logbook logbook = mock(Logbook.class);
    private final Http2AwareHandlerRegistrar registrar = new Http2AwareHandlerRegistrar(logbook);

    // --- Client observer tests ---

    @Test
    void shouldNotInstallClientHandlerOnH2TcpParentChannel() {
        Connection conn = mockConnectionWithHandler(Http2ConnectionHandler.class);
        registrar.clientObserver().onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldInstallClientHandlerOnHttp11Channel() {
        Connection conn = mockConnectionWithNoHandlers();
        registrar.clientObserver().onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldNotInstallClientHandlerTwiceOnKeepAliveHttp11Channel() {
        Connection conn = mockConnectionWithHandler(LogbookClientHandler.class);
        registrar.clientObserver().onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldInstallClientHandlerOnH2StreamChannel() {
        Connection conn = mockConnectionWithNoHandlers();
        registrar.clientObserver().onStateChange(conn, HttpClientState.STREAM_CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldIgnoreOtherClientStates() {
        Connection conn = mockConnectionWithNoHandlers();
        registrar.clientObserver().onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
        verify(conn, never()).addHandlerLast(any());
    }

    // --- Server observer tests ---

    @Test
    void shouldInstallServerHandlerOnNewConnection() {
        Connection conn = mockConnectionWithNoHandlers();
        registrar.serverObserver().onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookServerHandler.class));
    }

    @Test
    void shouldNotInstallServerHandlerTwiceOnKeepAliveChannel() {
        Connection conn = mockConnectionWithHandler(LogbookServerHandler.class);
        // First call: handler not present (handled by fresh mock)
        Connection freshConn = mockConnectionWithNoHandlers();
        registrar.serverObserver().onStateChange(freshConn, ConnectionObserver.State.CONFIGURED);

        // Second call: handler already present (keep-alive re-fire)
        registrar.serverObserver().onStateChange(conn, ConnectionObserver.State.CONFIGURED);

        verify(freshConn, times(1)).addHandlerLast(any(LogbookServerHandler.class));
        verify(conn, never()).addHandlerLast(any(LogbookServerHandler.class));
    }

    @Test
    void shouldIgnoreOtherServerStates() {
        Connection conn = mockConnectionWithNoHandlers();
        registrar.serverObserver().onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
        verify(conn, never()).addHandlerLast(any());
    }

    // --- helpers ---

    private Connection mockConnectionWithNoHandlers() {
        Connection conn = mock(Connection.class);
        Channel channel = mock(Channel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(conn.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        when(pipeline.get(Http2ConnectionHandler.class)).thenReturn(null);
        when(pipeline.get(LogbookClientHandler.class)).thenReturn(null);
        when(pipeline.get(LogbookServerHandler.class)).thenReturn(null);
        return conn;
    }

    private <T extends io.netty.channel.ChannelHandler> Connection mockConnectionWithHandler(Class<T> handlerClass) {
        Connection conn = mock(Connection.class);
        Channel channel = mock(Channel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(conn.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        T handler = mock(handlerClass);
        when(pipeline.get(handlerClass)).thenReturn(handler);
        return conn;
    }
}
