package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

final class LogbookServerHandlerTest {

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    @SuppressWarnings("deprecation") // the alternative method is not available prior to 1.0.0
    private final DisposableServer server = HttpServer.create()
            .tcpConfiguration(tcpServer ->
                    tcpServer.doOnConnection(connection ->
                            connection.addHandlerLast(new LogbookServerHandler(logbook))))
            .route(routes -> routes
                    .post("/discard", (request, response) ->
                            request.receive().aggregate().then())
                    .post("/echo", (request, response) ->
                            response.send(request.receive().aggregate()))
            )
            .bindNow();

    private final HttpClient client = HttpClient.create()
            .baseUrl("http://localhost:" + server.port());

    @BeforeEach
    void defaultBehaviour() {
        when(writer.isActive()).thenCallRealMethod();
    }

    @AfterEach
    void tearDown() {
        server.disposeNow();
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/echo HTTP/1.1", server.port()))
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        client.post()
                .uri("/discard")
                .send(helloWorld())
                .response()
                .block();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/discard HTTP/1.1", server.port()))
                .contains("Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException {
        when(writer.isActive()).thenReturn(false);

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        sendAndReceive("/discard");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        final String response = client.post()
                .uri("/echo")
                .send(helloWorld())
                .responseContent()
                .aggregate()
                .asString(UTF_8)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("Hello, world!");
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException {
        when(writer.isActive()).thenReturn(false);

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldIgnoreBodies() throws IOException {
        final String response = client.post()
                .uri("/echo")
                .send((request, outbound) -> {
                    request.addHeader("Ignore", "true");
                    return outbound.sendString(just("Hello, world!"));
                })
                .responseContent()
                .aggregate()
                .asString(UTF_8)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        {
            final String message = captureRequest();

            assertThat(message)
                    .startsWith("Incoming Request:")
                    .contains(format("POST http://localhost:%d/echo HTTP/1.1", server.port()))
                    .doesNotContain("Hello, world!");
        }

        {
            final String message = captureResponse();

            assertThat(message)
                    .startsWith("Outgoing Response:")
                    .contains("HTTP/1.1 200 OK")
                    .doesNotContain("Hello, world!");
        }
    }

    @Test
    void shouldWriteRequestWhenHandlerRemoved() throws Exception {
        when(writer.isActive()).thenReturn(true);
        final LogbookServerHandler handler = new LogbookServerHandler(logbook);

        final DefaultHttpRequest request = mock(DefaultHttpRequest.class);
        when(request.headers()).thenReturn(EmptyHttpHeaders.INSTANCE);
        when(request.uri()).thenReturn("/echo");
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        final ChannelHandlerContext context = mock();
        final Channel channel = mock(Channel.class);
        when(channel.pipeline()).thenReturn(mock());
        when(context.channel()).thenReturn(channel);

        // Simulate the channelRead event (not final)
        handler.channelRead(context, request);

        final FullHttpResponse message = mock(FullHttpResponse.class);
        when(message.headers()).thenReturn(EmptyHttpHeaders.INSTANCE);
        when(message.content()).thenReturn(new EmptyByteBuf(ByteBufAllocator.DEFAULT));
        when(message.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(message.status()).thenReturn(HttpResponseStatus.OK);

        // Simulate the write event (final)
        handler.write(mock(), message, mock());

        // Simulate the handlerRemoved event
        handler.handlerRemoved(mock());

        final String requestMessage = captureRequest();

        assertThat(requestMessage)
                .startsWith("Incoming Request:");

        final String responseMessage = captureResponse();

        assertThat(responseMessage)
                .startsWith("Outgoing Response:");
    }

    private void sendAndReceive() {
        sendAndReceive("/echo");
    }

    private static Mono<ByteBuf> helloWorld() {
        return just(wrappedBuffer("Hello, world!".getBytes(UTF_8)));
    }

    private void sendAndReceive(final String uri) {
        client.post()
                .uri(uri)
                .responseContent()
                .aggregate()
                .block();
    }

}
