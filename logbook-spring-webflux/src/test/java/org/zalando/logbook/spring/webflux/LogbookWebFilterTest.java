package org.zalando.logbook.spring.webflux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import org.zalando.logbook.*;

import java.io.IOException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.logbook.spring.webflux.LogbookWebFilterTest.*;

@SpringBootTest(classes = {TestApplication.class, FilterConfiguration.class}, webEnvironment = RANDOM_PORT)
@Import(MockitoExtension.class)
class LogbookWebFilterTest {

    @LocalServerPort
    int port;

    @MockBean
    public HttpLogWriter writer;

    private WebClient client;

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

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/discard HTTP/1.1", port))
                .contains("Hello, world!");
    }

    @Test
    void shouldLogRequestWithoutBody() throws IOException {
        sendAndReceive();

        final String message = captureRequest();

        assertThat(message)
                .startsWith("Incoming Request:")
                .contains(format("POST http://localhost:%d/echo HTTP/1.1", port))
                .doesNotContain("Hello, world!");
    }

    private String captureRequest() throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, timeout(1_000)).write(any(Precorrelation.class), captor.capture());
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
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
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
        verify(writer, timeout(1_000)).write(any(Correlation.class), captor.capture());
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
                .header("Ignore", "true")
                .bodyValue("Hello, world!")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("Hello, world!");

        {
            final String message = captureRequest();

            assertThat(message)
                    .startsWith("Incoming Request:")
                    .contains(format("POST http://localhost:%d/echo HTTP/1.1", port))
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
    void shouldLogGetResponseWithBody() throws IOException {
        final String response = client
                .get()
                .uri("/echo?q=test")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("test");

        final String reqMessage = captureRequest();
        assertThat(reqMessage)
                .startsWith("Incoming Request:")
                .contains(format("GET http://localhost:%d/echo?q=test HTTP/1.1", port));

        final String respMessage = captureResponse();
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

        final String reqMessage = captureRequest();
        assertThat(reqMessage)
                .startsWith("Incoming Request:")
                .contains(format("GET http://localhost:%d/empty HTTP/1.1", port));

        final String respMessage = captureResponse();
        assertThat(respMessage)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK");
    }

    private void sendAndReceive() {
        sendAndReceive("/echo");
    }

    private void sendAndReceive(final String uri) {
        client
                .post()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
