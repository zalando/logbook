package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        classes = LogbookWebFilterIntegrationTest.TestApplication.class,
        webEnvironment = RANDOM_PORT,
        properties = {
                "logbook.reactive.server-mode=web-filter",
                "spring.reactor.context-propagation=auto"
        })
class LogbookWebFilterIntegrationTest {

    @LocalServerPort
    private int port;

    /**
     * The trace id the application itself observed while handling the request, i.e. the value its own log lines
     * would have been tagged with.
     */
    private static final AtomicReference<String> applicationTraceId = new AtomicReference<>();

    @Autowired
    private WebFilter logbookServerFilter;

    @MockitoBean
    private HttpLogWriter writer;

    private WebClient client;

    @BeforeEach
    void setUp() {
        reset(writer);
        applicationTraceId.set(null);
        when(writer.isActive()).thenReturn(true);
        client = WebClient.create("http://localhost:" + port);
    }

    @Test
    void shouldRunBeforeSpringSecurityAndLogRejectedExchange() throws IOException {
        final HttpStatus status = client.get()
                .uri("/secured")
                .exchangeToMono(response -> response.releaseBody().thenReturn((HttpStatus) response.statusCode()))
                .block();

        assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(any(Correlation.class), anyString());
        assertThat(logbookServerFilter).isInstanceOfSatisfying(Ordered.class,
                filter -> assertThat(filter.getOrder()).isLessThan(-100));
    }

    @Test
    void shouldLogResponseFromRestControllerAdvice() throws IOException {
        final ResponseEntity<String> response = client.get()
                .uri("/failure")
                .exchangeToMono(clientResponse -> clientResponse.toEntity(String.class))
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("handled");
        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(
                any(Correlation.class),
                org.mockito.ArgumentMatchers.contains("handled"));
    }

    @Test
    void shouldWriteRequestAndResponseWithSameTraceIdAsTheApplication() throws IOException {
        final AtomicReference<String> requestTraceId = new AtomicReference<>();
        final AtomicReference<String> responseTraceId = new AtomicReference<>();
        doAnswer(invocation -> {
            requestTraceId.set(currentTraceId());
            return null;
        }).when(writer).write(any(Precorrelation.class), anyString());
        doAnswer(invocation -> {
            responseTraceId.set(currentTraceId());
            return null;
        }).when(writer).write(any(Correlation.class), anyString());

        final String response = client.get()
                .uri("/trace")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isEqualTo("traced");
        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(any(Correlation.class), anyString());

        assertThat(applicationTraceId.get()).isNotBlank();
        assertThat(requestTraceId.get()).isEqualTo(applicationTraceId.get());
        assertThat(responseTraceId.get()).isEqualTo(applicationTraceId.get());
    }

    /**
     * Reads the trace id the way an application does, i.e. from the MDC, which is what logging patterns such as
     * {@code %X{traceId}} render. This only works because {@code spring.reactor.context-propagation} is set to
     * {@code auto}; without it the tracing context stays in the Reactor context and never reaches the MDC.
     */
    private static String currentTraceId() {
        return MDC.get("traceId");
    }

    @SpringBootApplication
    @Import({SecurityConfiguration.class, TestController.class, TestExceptionHandler.class})
    static class TestApplication {
    }

    @Configuration(proxyBeanMethods = false)
    static class SecurityConfiguration {

        @Bean
        SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http
                    .authorizeExchange(exchange -> exchange
                            .pathMatchers("/secured").authenticated()
                            .anyExchange().permitAll())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/trace")
        String trace() {
            applicationTraceId.set(currentTraceId());
            return "traced";
        }

        @GetMapping("/failure")
        String failure() {
            throw new TestException();
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(TestException.class)
        ResponseEntity<String> handle() {
            return ResponseEntity.badRequest().body("handled");
        }
    }

    static class TestException extends RuntimeException {
    }
}
