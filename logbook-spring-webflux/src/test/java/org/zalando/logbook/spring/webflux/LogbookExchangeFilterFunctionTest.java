package org.zalando.logbook.spring.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.test.TestStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogbookExchangeFilterFunctionTest {

    private final WireMockServer server = new WireMockServer(options().dynamicPort());
    private final WireMockServer serverWithoutChunkEncoding = new WireMockServer(
            options()
                    .dynamicPort()
                    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );

    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .strategy(new TestStrategy())
            .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
            .build();

    private WebClient client;

    @BeforeEach
    void setup() {
        server.start();
        serverWithoutChunkEncoding.start();
        when(writer.isActive()).thenReturn(true);

        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(1000));
        client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(server.baseUrl())
                .filter(new LogbookExchangeFilterFunction(logbook))
                .codecs(it -> it.customCodecs().register(new Jackson2JsonEncoder(new ObjectMapper())))
                .codecs(it -> it.customCodecs().register(new Jackson2JsonDecoder(new ObjectMapper())))
                .build();
    }

    @AfterEach
    void tearDown() {
        server.stop();
        serverWithoutChunkEncoding.stop();
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        server.stubFor(post("/echo").willReturn(aResponse().withStatus(200)));

        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST %s/echo HTTP/1.1", server.baseUrl()))
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogEmptyResponseWithTransferEncodingChunked() throws IOException {
        server.stubFor(get("/empty-chunked").willReturn(aResponse().withStatus(400)));

        assertThatThrownBy(() -> client
                .get()
                .uri(server.baseUrl() + "/empty-chunked")
                .retrieve()
                .toBodilessEntity()
                .block())
                .isInstanceOf(WebClientResponseException.class)
                .hasMessage(format("400 Bad Request from GET %s/empty-chunked", server.baseUrl()));

        final String message = captureRequest();
        final String response = captureResponse();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("GET %s/empty-chunked HTTP/1.1", server.baseUrl()))
                .doesNotContain("Hello, world!");

        assertThat(response)
                .startsWith("Incoming Response:")
                .contains("Transfer-Encoding: chunked")
                .contains("HTTP/1.1 400 Bad Request");
    }

    @Test
    void shouldLogRequestWithBody() throws IOException {
        server.stubFor(post("/discard").willReturn(aResponse().withStatus(200)));

        client.post()
                .uri("/discard")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST %s/discard HTTP/1.1", server.baseUrl()))
                .contains("Hello, world!");
    }

    @Test
    void shouldLogRequestOnTimeout() throws IOException {
        server.stubFor(post("/discard").willReturn(aResponse().withStatus(200).withFixedDelay(1000)));

        client.post()
                .uri("/discard")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorComplete()
                .block();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Outgoing Request:")
                .contains(format("POST %s/discard HTTP/1.1", server.baseUrl()))
                .contains("Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogRequestIfInactive() throws IOException {
        server.stubFor(post("/echo").willReturn(aResponse().withStatus(200)));

        when(writer.isActive()).thenReturn(false);

        sendAndReceive();

        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseWithoutBody() throws IOException {
        serverWithoutChunkEncoding.stubFor(post("/discard").willReturn(aResponse().withStatus(200)));

        client
                .post()
                .uri(serverWithoutChunkEncoding.baseUrl() + "/discard")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length: 0")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldLogResponseWithBody() throws IOException {
        serverWithoutChunkEncoding.stubFor(post("/echo").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        final String response = client.post()
                .uri(serverWithoutChunkEncoding.baseUrl() + "/echo")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("Content-Length: 13")
                .contains("Hello, world!");
    }

    private String captureResponse() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, timeout(1_000)).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldNotLogResponseIfInactive() throws IOException {
        server.stubFor(post("/echo").willReturn(aResponse().withStatus(200)));

        when(writer.isActive()).thenReturn(false);

        sendAndReceive();

        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldLogChunkedResponseWithBody() throws IOException {
        server.stubFor(get("/chunked").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        final String response = client.get()
                .uri("/chunked")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        final String message = captureResponse();

        assertThat(message)
                .startsWith("Incoming Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("Transfer-Encoding: chunked")
                .contains("Hello, world!");
    }

    @Test
    void shouldReplayConsumedResponseBodyWithoutResubscribing() throws IOException {
        final AtomicInteger subscriptions = new AtomicInteger();
        final byte[] expected = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        final ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Length", String.valueOf(expected.length))
                .body(Flux.defer(() -> {
                    if (subscriptions.incrementAndGet() == 1) {
                        return Flux.just(
                                DefaultDataBufferFactory.sharedInstance.wrap(expected)
                        );
                    }
                    return Flux.error(new IllegalStateException(
                            "The client response body can only be consumed once."
                    ));
                }))
                .build();
        final ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/test")).build();
        final ExchangeFunction exchange = ignored -> Mono.just(response);

        final ClientResponse returned = new LogbookExchangeFilterFunction(logbook).filter(request, exchange).block();
        final byte[] actual = returned.bodyToMono(byte[].class).block();

        assertThat(actual).isEqualTo(expected);
        assertThat(subscriptions).hasValue(1);

        captureRequest();
        assertThat(captureResponse()).contains("Hello, world!");
    }

    @Test
    void shouldReplayConsumedEmptyResponseWithoutResubscribing() throws IOException {
        final AtomicInteger subscriptions = new AtomicInteger();
        final ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Transfer-Encoding", "chunked")
                .body(Flux.defer(() -> {
                    if (subscriptions.incrementAndGet() == 1) {
                        return Flux.empty();
                    }
                    return Flux.error(new IllegalStateException(
                            "The client response body can only be consumed once."
                    ));
                }))
                .build();
        final ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("https://example.com/empty")).build();
        final ExchangeFunction exchange = ignored -> Mono.just(response);

        final ClientResponse returned = new LogbookExchangeFilterFunction(logbook).filter(request, exchange).block();
        final List<DataBuffer> body = returned
                .bodyToFlux(DataBuffer.class)
                .collectList()
                .block();

        assertThat(body).isEmpty();
        assertThat(subscriptions).hasValue(1);

        captureRequest();
        assertThat(captureResponse())
                .contains("HTTP/1.1 200 OK")
                .contains("Transfer-Encoding: chunked")
                .doesNotContain("Hello, world!");
    }

    @Test
    void shouldIgnoreBodies() throws IOException {
        server.stubFor(post("/echo").willReturn(aResponse().withStatus(200).withBody("Hello, world!")));

        final String response = client.post()
                .uri("/echo")
                .header("Ignore", "true")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        {
            final String message = captureRequest();

            assertThat(message)
                    .startsWith("Outgoing Request:")
                    .contains(format("POST %s/echo HTTP/1.1", server.baseUrl()))
                    .doesNotContain("Hello, world!");
        }

        {
            final String message = captureResponse();

            assertThat(message)
                    .startsWith("Incoming Response:")
                    .contains("HTTP/1.1 200 OK")
                    .doesNotContain("Hello, world!");
        }
    }

    private String sendAndReceive() {
        return sendAndReceive("/echo");
    }

    private String sendAndReceive(final String uri) {
        return client
                .post()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
