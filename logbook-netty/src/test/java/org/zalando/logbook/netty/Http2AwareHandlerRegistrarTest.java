package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Logbook;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.http.server.HttpServer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Http2AwareHandlerRegistrarTest {

    private final Logbook logbook = mock(Logbook.class);

    // --- Client tests ---

    @Test
    void shouldNotInstallClientHandlerOnH2TcpParentChannel() {
        ConnectionObserver observer = captureClientObserver();
        Connection conn = mockConnectionWithHandler(Http2ConnectionHandler.class);
        observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldInstallClientHandlerOnHttp11Channel() {
        ConnectionObserver observer = captureClientObserver();
        Connection conn = mockConnectionWithNoHandlers();
        observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldNotInstallClientHandlerTwiceOnKeepAliveHttp11Channel() {
        ConnectionObserver observer = captureClientObserver();
        Connection conn = mockConnectionWithHandler(LogbookClientHandler.class);
        observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldInstallClientHandlerOnH2StreamChannel() {
        ConnectionObserver observer = captureClientObserver();
        Connection conn = mockConnectionWithNoHandlers();
        observer.onStateChange(conn, HttpClientState.STREAM_CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
    }

    @Test
    void shouldIgnoreOtherClientStates() {
        ConnectionObserver observer = captureClientObserver();
        Connection conn = mockConnectionWithNoHandlers();
        observer.onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
        verify(conn, never()).addHandlerLast(any());
    }

    // --- Server tests ---

    @Test
    void shouldInstallServerHandlerOnNewConnection() {
        ConnectionObserver observer = captureServerObserver();
        Connection conn = mockConnectionWithNoHandlers();
        observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
        verify(conn, times(1)).addHandlerLast(any(LogbookServerHandler.class));
    }

    @Test
    void shouldNotInstallServerHandlerTwiceOnKeepAliveChannel() {
        ConnectionObserver observer = captureServerObserver();

        Connection freshConn = mockConnectionWithNoHandlers();
        observer.onStateChange(freshConn, ConnectionObserver.State.CONFIGURED);

        Connection existingConn = mockConnectionWithHandler(LogbookServerHandler.class);
        observer.onStateChange(existingConn, ConnectionObserver.State.CONFIGURED);

        verify(freshConn, times(1)).addHandlerLast(any(LogbookServerHandler.class));
        verify(existingConn, never()).addHandlerLast(any(LogbookServerHandler.class));
    }

    @Test
    void shouldIgnoreOtherServerStates() {
        ConnectionObserver observer = captureServerObserver();
        Connection conn = mockConnectionWithNoHandlers();
        observer.onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
        verify(conn, never()).addHandlerLast(any());
    }

    // --- helpers ---

    private ConnectionObserver captureClientObserver() {
        HttpClient base = mock(HttpClient.class);
        ArgumentCaptor<ConnectionObserver> captor = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(base.observe(captor.capture())).thenReturn(mock(HttpClient.class));
        Http2AwareHandlerRegistrar.installOnClient(base, logbook);
        return captor.getValue();
    }

    private ConnectionObserver captureServerObserver() {
        HttpServer base = mock(HttpServer.class);
        ArgumentCaptor<ConnectionObserver> captor = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(base.observe(captor.capture())).thenReturn(mock(HttpServer.class));
        Http2AwareHandlerRegistrar.installOnServer(base, logbook);
        return captor.getValue();
    }

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

    private <T extends ChannelHandler> Connection mockConnectionWithHandler(Class<T> handlerClass) {
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
