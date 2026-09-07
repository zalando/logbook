package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.ConnectionProvider;

import java.io.IOException;
import java.time.Duration;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Http2AwareHandlerRegistrarTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private DisposableServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow(TIMEOUT);
        }
    }

    @Test
    void shouldLogRealH2cGetRequestAndResponse() {
        final CapturingWriter clientWriter = new CapturingWriter();
        final CapturingWriter serverWriter = new CapturingWriter();
        server = startH2cServer(serverWriter);

        final String response = h2cClient(clientWriter)
                .get()
                .uri("/h2c-get")
                .responseSingle((ignored, content) -> content.asString())
                .block(TIMEOUT);

        assertThat(response).isEqualTo("h2c-get-response");
        awaitMessages(clientWriter, 2);
        awaitMessages(serverWriter, 2);
        assertThat(clientWriter.requests).anySatisfy(message -> assertThat(message)
                .contains("GET", "/h2c-get", "HTTP/2.0"));
        assertThat(clientWriter.responses).anySatisfy(message -> assertThat(message)
                .contains("HTTP/2.0 200 OK"));
        assertThat(serverWriter.requests).anySatisfy(message -> assertThat(message)
                .contains("GET", "/h2c-get", "HTTP/2.0"));
        assertThat(serverWriter.responses).anySatisfy(message -> assertThat(message)
                .contains("HTTP/2.0 200 OK"));
    }

    @Test
    void shouldLogH2cBodiesAndCleanHeaders() {
        final CapturingWriter clientWriter = new CapturingWriter();
        final CapturingWriter serverWriter = new CapturingWriter();
        server = startH2cServer(serverWriter);

        final String response = h2cClient(clientWriter)
                .post()
                .uri("/h2c-body")
                .send((request, outbound) -> {
                    request.addHeader("X-Logbook-User", "present");
                    return outbound.send(Mono.just(Unpooled.wrappedBuffer("request-body".getBytes(UTF_8))));
                })
                .responseSingle((ignored, content) -> content.asString())
                .block(TIMEOUT);

        assertThat(response).isEqualTo("response-body");
        awaitMessages(clientWriter, 2);
        awaitMessages(serverWriter, 2);
        assertH2cRequest(clientWriter.requests, "request-body");
        assertH2cRequest(serverWriter.requests, "request-body");
        assertH2cResponse(clientWriter.responses, "response-body");
        assertH2cResponse(serverWriter.responses, "response-body");
    }

    @Test
    void shouldHandleH2cAndHttp11OnOneServer() {
        final CapturingWriter serverWriter = new CapturingWriter();
        final CapturingWriter h2cWriter = new CapturingWriter();
        final CapturingWriter http11Writer = new CapturingWriter();
        final LifecycleErrorObserver errors = new LifecycleErrorObserver();
        final ConnectionProvider connections = ConnectionProvider.create("dual-protocol-test");
        try {
            server = startDualProtocolServer(serverWriter, errors);

            assertThat(h2cClient(h2cWriter, connections, errors)
                    .get()
                    .uri("/h2c-dual")
                    .responseSingle((ignored, content) -> content.asString())
                    .block(TIMEOUT)).isEqualTo("h2c-dual-response");
            assertThat(http11Client(http11Writer, connections, errors)
                    .get()
                    .uri("/http11-dual")
                    .responseSingle((ignored, content) -> content.asString())
                    .block(TIMEOUT)).isEqualTo("http11-dual-response");

            awaitMessages(serverWriter, 4);
            awaitMessages(h2cWriter, 2);
            awaitMessages(http11Writer, 2);
            assertProtocolPair(h2cWriter, "/h2c-dual", "HTTP/2.0");
            assertProtocolPair(http11Writer, "/http11-dual", "HTTP/1.1");
            assertProtocolPair(serverWriter, "/h2c-dual", "HTTP/2.0");
            assertProtocolPair(serverWriter, "/http11-dual", "HTTP/1.1");
        } finally {
            if (server != null) {
                server.disposeNow(TIMEOUT);
                server = null;
            }
            connections.disposeLater().block(TIMEOUT);
        }
        errors.assertNoRejectedExecutionException();
    }

    @Nested
    class Client {

        @Test
        void shouldInstallClientHandlerOnH2StreamChannel() {
            ConnectionObserver observer = captureClientObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithNoHandlers();
            observer.onStateChange(conn, HttpClientState.STREAM_CONFIGURED);
            verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
        }

        @Test
        void shouldNotInstallClientHandlerOnH2TcpParentChannel() {
            ConnectionObserver observer = captureClientObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithHandler(Http2ConnectionHandler.class);
            observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
            verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
        }

        @Test
        void shouldInstallClientHandlerOnHttp11Channel() {
            ConnectionObserver observer = captureClientObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithNoHandlers();
            observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
            verify(conn, times(1)).addHandlerLast(any(LogbookClientHandler.class));
        }

        @Test
        void shouldNotInstallClientHandlerTwiceOnKeepAliveHttp11Channel() {
            ConnectionObserver observer = captureClientObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithHandler(LogbookClientHandler.class);
            observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
            verify(conn, never()).addHandlerLast(any(LogbookClientHandler.class));
        }

        @Test
        void shouldIgnoreOtherClientStates() {
            ConnectionObserver observer = captureClientObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithNoHandlers();
            observer.onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
            verify(conn, never()).addHandlerLast(any());
        }
    }

    @Nested
    class Server {

        @Test
        void shouldInstallServerHandlerOnConfiguredConnection() {
            ConnectionObserver observer = captureServerObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithNoHandlers();
            observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
            verify(conn, times(1)).addHandlerLast(any(LogbookServerHandler.class));
        }

        @Test
        void shouldNotInstallServerHandlerTwiceOnKeepAliveChannel() {
            ConnectionObserver observer = captureServerObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithHandler(LogbookServerHandler.class);
            observer.onStateChange(conn, ConnectionObserver.State.CONFIGURED);
            verify(conn, never()).addHandlerLast(any(LogbookServerHandler.class));
        }

        @Test
        void shouldIgnoreOtherServerStates() {
            ConnectionObserver observer = captureServerObserver(mock(Logbook.class));
            Connection conn = mockConnectionWithNoHandlers();
            observer.onStateChange(conn, ConnectionObserver.State.DISCONNECTING);
            verify(conn, never()).addHandlerLast(any());
        }
    }

    private ConnectionObserver captureClientObserver(Logbook logbook) {
        HttpClient base = mock(HttpClient.class);
        ArgumentCaptor<ConnectionObserver> captor = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(base.observe(captor.capture())).thenReturn(mock(HttpClient.class));
        Http2AwareHandlerRegistrar.installOnClient(base, logbook);
        return captor.getValue();
    }

    private ConnectionObserver captureServerObserver(Logbook logbook) {
        HttpServer base = mock(HttpServer.class);
        ArgumentCaptor<ConnectionObserver> captor = ArgumentCaptor.forClass(ConnectionObserver.class);
        when(base.childObserve(captor.capture())).thenReturn(mock(HttpServer.class));
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
        when(pipeline.toMap()).thenReturn(Map.of());
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
        when(pipeline.toMap()).thenReturn(Map.of("handler", handler));
        return conn;
    }

    private DisposableServer startH2cServer(final CapturingWriter writer) {
        return Http2AwareHandlerRegistrar.installOnServer(HttpServer.create()
                        .protocol(HttpProtocol.H2C)
                        .route(routes -> routes
                                .get("/h2c-get", (request, response) ->
                                        response.sendString(Mono.just("h2c-get-response")))
                                .post("/h2c-body", (request, response) ->
                                        request.receive().aggregate().then(response
                                                .header("X-Logbook-User", "present")
                                                .sendString(Mono.just("response-body")).then()))), logbook(writer))
                .bindNow(TIMEOUT);
    }

    private HttpClient h2cClient(final CapturingWriter writer) {
        return Http2AwareHandlerRegistrar.installOnClient(HttpClient.create()
                        .protocol(HttpProtocol.H2C)
                        .baseUrl("http://localhost:" + server.port()), logbook(writer));
    }

    private HttpClient h2cClient(
            final CapturingWriter writer,
            final ConnectionProvider connections,
            final ConnectionObserver errors) {
        return Http2AwareHandlerRegistrar.installOnClient(HttpClient.create(connections)
                        .protocol(HttpProtocol.H2C)
                        .observe(errors)
                        .baseUrl("http://localhost:" + server.port()), logbook(writer));
    }

    private HttpClient http11Client(final CapturingWriter writer) {
        return Http2AwareHandlerRegistrar.installOnClient(HttpClient.create()
                        .protocol(HttpProtocol.HTTP11)
                        .baseUrl("http://localhost:" + server.port()), logbook(writer));
    }

    private HttpClient http11Client(
            final CapturingWriter writer,
            final ConnectionProvider connections,
            final ConnectionObserver errors) {
        return Http2AwareHandlerRegistrar.installOnClient(HttpClient.create(connections)
                        .protocol(HttpProtocol.HTTP11)
                        .observe(errors)
                        .baseUrl("http://localhost:" + server.port()), logbook(writer));
    }

    private DisposableServer startDualProtocolServer(
            final CapturingWriter writer,
            final ConnectionObserver errors) {
        return Http2AwareHandlerRegistrar.installOnServer(HttpServer.create()
                        .protocol(HttpProtocol.H2C, HttpProtocol.HTTP11)
                        .childObserve(errors)
                        .route(routes -> routes
                                .get("/h2c-dual", (request, response) ->
                                        response.sendString(Mono.just("h2c-dual-response")))
                                .get("/http11-dual", (request, response) ->
                                        response.sendString(Mono.just("http11-dual-response")))), logbook(writer))
                .bindNow(TIMEOUT);
    }

    private Logbook logbook(final HttpLogWriter writer) {
        return Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
    }

    private void awaitMessages(final CapturingWriter writer, final int expected) {
        final long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (writer.size() < expected && System.nanoTime() < deadline) {
            LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
        }
        assertThat(writer.size()).isGreaterThanOrEqualTo(expected);
    }

    private void assertH2cRequest(final Queue<String> messages, final String body) {
        assertThat(messages).anySatisfy(message -> assertThat(message)
                .contains("HTTP/2.0", body)
                .containsIgnoringCase("x-logbook-user: present"));
        assertNoExtensionHeaders(messages);
    }

    private void assertH2cResponse(final Queue<String> messages, final String body) {
        assertThat(messages).anySatisfy(message -> assertThat(message)
                .contains("HTTP/2.0 200 OK", body)
                .containsIgnoringCase("x-logbook-user: present"));
        assertNoExtensionHeaders(messages);
    }

    private void assertNoExtensionHeaders(final Queue<String> messages) {
        for (final HttpConversionUtil.ExtensionHeaderNames header : HttpConversionUtil.ExtensionHeaderNames.values()) {
            assertThat(messages).allSatisfy(message -> assertThat(message).doesNotContain(header.text()));
        }
    }

    private void assertProtocolPair(
            final CapturingWriter writer,
            final String path,
            final String protocol) {
        assertThat(writer.requests).anySatisfy(message -> assertThat(message).contains(path, protocol));
        assertThat(writer.responses).anySatisfy(message -> assertThat(message).contains(protocol, "200 OK"));
    }

    private static final class CapturingWriter implements HttpLogWriter {

        private final Queue<String> requests = new ConcurrentLinkedQueue<>();
        private final Queue<String> responses = new ConcurrentLinkedQueue<>();

        @Override
        public void write(final Precorrelation correlation, final String request) throws IOException {
            requests.add(request);
        }

        @Override
        public void write(final Correlation correlation, final String response) throws IOException {
            responses.add(response);
        }

        private int size() {
            return requests.size() + responses.size();
        }

    }

    private static final class LifecycleErrorObserver implements ConnectionObserver {

        private final Queue<Throwable> errors = new ConcurrentLinkedQueue<>();

        @Override
        public void onUncaughtException(final Connection connection, final Throwable error) {
            errors.add(error);
        }

        @Override
        public void onStateChange(final Connection connection, final State state) {
            // Errors are the only lifecycle signal relevant to this test.
        }

        private void assertNoRejectedExecutionException() {
            assertThat(errors).noneSatisfy(error -> assertThat(rejectedExecutionException(error)).isNotNull());
        }

        private static RejectedExecutionException rejectedExecutionException(final Throwable error) {
            for (Throwable cause = error; cause != null; cause = cause.getCause()) {
                if (cause instanceof RejectedExecutionException) {
                    return (RejectedExecutionException) cause;
                }
            }
            return null;
        }
    }
}
