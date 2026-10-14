package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Calls the application, which makes a call of its own, and checks that the outgoing request and the response to it
 * are logged under the trace id of the call that caused them.
 *
 * <p>Incoming logging is disabled so that only the outgoing entries reach the writer.
 */
@SpringBootTest(
        classes = LogbookNettyClientTracingIntegrationTest.TestApplication.class,
        webEnvironment = RANDOM_PORT,
        properties = {
                "logbook.filter.enabled=false",
                // so the handler method can read the trace id it is meant to be compared against
                "spring.reactor.context-propagation=auto"
        })
class LogbookNettyClientTracingIntegrationTest {

    /**
     * What {@code /outbound} reports when the application's own MDC holds no trace id.
     */
    static final String NO_TRACE_ID = "none";

    @LocalServerPort
    private int port;

    @MockitoBean
    private HttpLogWriter writer;

    @Autowired
    private RemotePorts remotePorts;

    private final List<String> loggedTraceIds = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        reset(writer);
        loggedTraceIds.clear();
        remotePorts.values().clear();
        when(writer.isActive()).thenReturn(true);

        doAnswer(invocation -> loggedTraceIds.add(currentTraceId()))
                .when(writer).write(any(Precorrelation.class), anyString());
        doAnswer(invocation -> loggedTraceIds.add(currentTraceId()))
                .when(writer).write(any(Correlation.class), anyString());
    }

    @Test
    void shouldWriteRequestAndResponseWithSameTraceIdAsTheCallingExchange() throws IOException {
        final String callerTraceId = callOutbound(port);

        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(any(Correlation.class), anyString());

        assertThat(callerTraceId)
                .describedAs("trace id the request handler itself observed")
                .isNotEqualTo(NO_TRACE_ID);
        assertThat(loggedTraceIds)
                .describedAs("trace id in the MDC while the outgoing request and its response were written")
                .containsExactly(callerTraceId, callerTraceId);
    }

    /**
     * The trace is read off the channel at the moment of writing, so a connection handed back to the pool and picked
     * up by an unrelated request must be logged under the new trace rather than the one before it.
     */
    @Test
    void shouldKeepTracesApartWhenTheConnectionIsReused() throws IOException {
        final String firstTraceId = callOutbound(port);
        verify(writer, timeout(5_000).times(1)).write(any(Correlation.class), anyString());

        final String secondTraceId = callOutbound(port);
        verify(writer, timeout(5_000).times(2)).write(any(Correlation.class), anyString());

        assertThat(remotePorts.values())
                .describedAs("source port of the outgoing connection, the same only if the pool reused it")
                .hasSize(2)
                .containsOnly(remotePorts.values().get(0));
        assertThat(firstTraceId)
                .describedAs("the two calls arrive under traces of their own")
                .isNotEqualTo(secondTraceId);
        assertThat(loggedTraceIds)
                .describedAs("trace id in the MDC for the request and response of each call")
                .containsExactly(firstTraceId, firstTraceId, secondTraceId, secondTraceId);
    }

    /**
     * It is the client observation that puts the trace onto the channel, so a {@code WebClient} built without one
     * leaves nothing to restore. The entries are still written, only without a trace id.
     */
    @Test
    void shouldStillLogWithoutATraceIdWhenTheClientIsNotObserved() throws IOException {
        final String callerTraceId = callOutbound(port, "/outbound-unobserved");

        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(any(Correlation.class), anyString());

        assertThat(callerTraceId)
                .describedAs("trace id the request handler itself observed")
                .isNotEqualTo(NO_TRACE_ID);
        assertThat(loggedTraceIds)
                .describedAs("nothing reaches the channel, so the entries carry no trace id")
                .containsExactly(null, null);
    }

    /**
     * The trace id as a logging pattern such as {@code %X{traceId}} would see it.
     */
    static String currentTraceId() {
        return MDC.get("traceId");
    }

    /**
     * Calls the application, which makes a call of its own, and reports the trace id it saw while doing so.
     */
    static String callOutbound(final int port) {
        return callOutbound(port, "/outbound");
    }

    static String callOutbound(final int port, final String path) {
        return WebClient.create("http://localhost:" + port)
                .get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Source port of every call that reached {@code /echo}. Two calls share a port only if the connection pool
     * handed out the same connection twice.
     */
    static final class RemotePorts {

        private final List<Integer> values = new CopyOnWriteArrayList<>();

        List<Integer> values() {
            return values;
        }
    }

    @SpringBootApplication
    @Import({TestConfiguration.class, TestController.class})
    static class TestApplication {
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        RemotePorts remotePorts() {
            return new RemotePorts();
        }

        @Bean
        SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }

    @RestController
    static class TestController {

        // the connector Logbook has added its handler to
        private final WebClient client;

        // the same connector, but with no observations, which is the case the README warns about
        private final WebClient unobservedClient;

        private final Environment environment;

        private final RemotePorts remotePorts;

        TestController(final ClientHttpConnector connector,
                       final ObservationRegistry observations,
                       final Environment environment,
                       final RemotePorts remotePorts) {
            // Spring Boot's own builder does this too, and without it there is no trace on the channel
            this.client = WebClient.builder()
                    .clientConnector(connector)
                    .observationRegistry(observations)
                    .build();
            this.unobservedClient = WebClient.builder()
                    .clientConnector(connector)
                    .build();
            this.environment = environment;
            this.remotePorts = remotePorts;
        }

        @GetMapping("/outbound")
        Mono<String> outbound() {
            // may be absent, the application's own MDC is only populated under automatic context propagation
            final String traceId = Objects.toString(currentTraceId(), NO_TRACE_ID);
            return client.get()
                    .uri("http://localhost:" + environment.getProperty("local.server.port") + "/echo")
                    .retrieve()
                    .bodyToMono(String.class)
                    .thenReturn(traceId);
        }

        @GetMapping("/outbound-unobserved")
        Mono<String> outboundUnobserved() {
            final String traceId = Objects.toString(currentTraceId(), NO_TRACE_ID);
            return unobservedClient.get()
                    .uri("http://localhost:" + environment.getProperty("local.server.port") + "/echo")
                    .retrieve()
                    .bodyToMono(String.class)
                    .thenReturn(traceId);
        }

        @GetMapping("/echo")
        String echo(final ServerWebExchange exchange) {
            remotePorts.values().add(exchange.getRequest().getRemoteAddress().getPort());
            return "echo";
        }
    }
}
