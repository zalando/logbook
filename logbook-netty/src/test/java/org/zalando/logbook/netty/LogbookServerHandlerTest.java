package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.TestStrategy;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
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

        assertThat(message, startsWith("Incoming Request:"));
        assertThat(message, containsString(format("POST http://localhost:%d/echo HTTP/1.1", server.port())));
        assertThat(message, not(containsString("Hello, world!")));
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        client.post()
                .uri("/discard")
                .send(helloWorld())
                .response()
                .block();

        final String message = captureRequest();

        assertThat(message, startsWith("Incoming Request:"));
        assertThat(message, containsString(format("POST http://localhost:%d/discard HTTP/1.1", server.port())));
        assertThat(message, containsString("Hello, world!"));
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

        assertThat(message, startsWith("Outgoing Response:"));
        assertThat(message, containsString("HTTP/1.1 200 OK"));
        assertThat(message, not(containsString("Hello, world!")));
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

        assertThat(response, is("Hello, world!"));

        final String message = captureResponse();

        assertThat(message, startsWith("Outgoing Response:"));
        assertThat(message, containsString("HTTP/1.1 200 OK"));
        assertThat(message, containsString("Hello, world!"));
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

        assertThat(response, is("Hello, world!"));

        {
            final String message = captureRequest();

            assertThat(message, startsWith("Incoming Request:"));
            assertThat(message, containsString(format("POST http://localhost:%d/echo HTTP/1.1", server.port())));
            assertThat(message, not(containsString("Hello, world!")));
        }

        {
            final String message = captureResponse();

            assertThat(message, startsWith("Outgoing Response:"));
            assertThat(message, containsString("HTTP/1.1 200 OK"));
            assertThat(message, not(containsString("Hello, world!")));
        }
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
