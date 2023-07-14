package org.zalando.logbook.spring.webflux;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.core.WithoutBodyStrategy;
import org.zalando.logbook.test.TestStrategy;

import java.io.IOException;
import reactor.core.publisher.Mono;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

class LogbookWebFilterTest {

    @TestConfiguration
    static class FilterConfiguration {

        @Bean
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public Logbook logbook(HttpLogWriter writer) {
            return Logbook.builder()
                .strategy(new TestStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        }

        @Bean
        public WebFilter filter(Logbook logbook) {
            return new LogbookWebFilter(logbook);
        }
    }

    @TestConfiguration
    static class FilterWithoutBodyConfiguration {

        @Bean
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public Logbook logbook(HttpLogWriter writer) {
            return Logbook.builder()
                .strategy(new WithoutBodyStrategy())
                .sink(new DefaultSink(new DefaultHttpLogFormatter(), writer))
                .build();
        }

        @Bean
        public WebFilter filter(Logbook logbook) {
            return new LogbookWebFilter(logbook);
        }
    }

    @Nested
    @Import(MockitoExtension.class)
    @SpringBootTest(classes = {TestApplication.class, FilterConfiguration.class}, webEnvironment = RANDOM_PORT)
    class WithTestStrategy {

        private WebClient client;

        @MockBean
        public HttpLogWriter writer;

        @LocalServerPort
        int port;

        @BeforeEach
        void setup() {
            when(writer.isActive()).thenReturn(true);
            client = WebClient.builder()
                .baseUrl(String.format("http://localhost:%d", port))
                .build();
        }

        @Test
        void shouldLogRequestWithBody() throws IOException {
            client.post()
                .uri("/discard")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            final String message = captureRequest(writer);

            assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/discard HTTP/1.1", port))
                .contains("Hello, world!");
        }

        @Test
        void shouldLogRequestWithoutBody() throws IOException {
            sendAndReceive(client);

            final String message = captureRequest(writer);

            assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/echo HTTP/1.1", port))
                .doesNotContain("Hello, world!");
        }

        @Test
        void shouldNotLogRequestIfInactive() throws IOException {
            when(writer.isActive()).thenReturn(false);

            sendAndReceive(client);

            verify(writer, never()).write(any(Precorrelation.class), any());
        }

        @Test
        void shouldLogResponseWithoutBody() throws IOException {
            sendAndReceive(client, "/discard");

            final String message = captureResponse(writer);

            assertThat(message)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK")
                .doesNotContain("Hello, world!");
        }

        @Test
        void shouldLogResponseWithBody() throws IOException {
            final String response = client.post()
                .uri("/echo")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            assertThat(response).isEqualTo("Hello, world!");

            final String message = captureResponse(writer);

            assertThat(message)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK")
                .contains("Hello, world!");
        }

        @Test
        void shouldNotLogResponseIfInactive() throws IOException {
            when(writer.isActive()).thenReturn(false);

            sendAndReceive(client);

            verify(writer, never()).write(any(Correlation.class), any());
        }

        @Test
        void shouldIgnoreBodies() throws IOException {
            final String response = client.post()
                .uri("/echo")
                .header("Ignore", "true")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            assertThat(response).isEqualTo("Hello, world!");

            {
                final String message = captureRequest(writer);

                assertThat(message)
                    .startsWith("Incoming Request:")
                    .contains(format("POST http://localhost:%d/echo HTTP/1.1", port))
                    .doesNotContain("Hello, world!");
            }

            {
                final String message = captureResponse(writer);

                assertThat(message)
                    .startsWith("Outgoing Response:")
                    .contains("HTTP/1.1 200 OK")
                    .doesNotContain("Hello, world!");
            }
        }

        @Test
        void shouldLogGetResponseWithBody() throws IOException {
            final String response = client
                .get()
                .uri("/echo?q=test")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            assertThat(response).isEqualTo("test");

            final String reqMessage = captureRequest(writer);
            assertThat(reqMessage)
                .startsWith("Incoming Request:")
                .contains(format("GET http://localhost:%d/echo?q=test HTTP/1.1", port));

            final String respMessage = captureResponse(writer);
            assertThat(respMessage)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK");
        }

        @Test
        void shouldLogGetResponseWithoutBody() throws IOException {
            client
                .get()
                .uri("/empty")
                .retrieve()
                .toBodilessEntity()
                .block();

            final String reqMessage = captureRequest(writer);
            assertThat(reqMessage)
                .startsWith("Incoming Request:")
                .contains(format("GET http://localhost:%d/empty HTTP/1.1", port));

            final String respMessage = captureResponse(writer);
            assertThat(respMessage)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK");
        }
    }

    @Nested
    @SpringBootTest(classes = {TestApplication.class, FilterWithoutBodyConfiguration.class}, webEnvironment = RANDOM_PORT)
    @Import(MockitoExtension.class)
    class WithoutBody {

        private WebClient client;

        @MockBean
        public HttpLogWriter writer;

        @LocalServerPort
        int port;

        @BeforeEach
        void setup() {
            when(writer.isActive()).thenReturn(true);
            client = WebClient.builder()
                .baseUrl(String.format("http://localhost:%d", port))
                .build();
        }

        @Test
        void shouldLogRequestWithoutBody() throws IOException {
            sendAndReceive(client);

            final String message = captureRequest(writer);

            assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/echo HTTP/1.1", port))
                .doesNotContain("Hello, world!");
        }
    }

    @Nested
    class WithMockedObjects {
        @Test
        void shouldNotCallWriteOnInappropriateStage() {
            final Logbook logbook = mock(Logbook.class);
            final LogbookWebFilter underTest = new LogbookWebFilter(logbook);

            final WebFilterChain chain = new DefaultWebFilterChain(
                filteredExchange ->
                    Mono.fromCallable(() -> filteredExchange.getResponse().setStatusCode(HttpStatus.OK))
                        .then(filteredExchange.getResponse().setComplete()),
                Collections.singletonList(underTest));
            final ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

            chain.filter(exchange).block();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    private void sendAndReceive(final WebClient client) {
        sendAndReceive(client, "/echo");
    }

    private void sendAndReceive(final WebClient client, final String uri) {
        client
            .post()
            .uri(uri)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private String captureRequest(final HttpLogWriter writer) throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, timeout(1_000)).write(any(Precorrelation.class), captor.capture());
        return captor.getValue();
    }

    private String captureResponse(final HttpLogWriter writer) throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, timeout(1_000)).write(any(Correlation.class), captor.capture());
        return captor.getValue();
    }
}
