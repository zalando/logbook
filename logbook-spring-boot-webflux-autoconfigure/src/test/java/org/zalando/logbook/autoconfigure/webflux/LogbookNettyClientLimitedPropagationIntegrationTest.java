package org.zalando.logbook.autoconfigure.webflux;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;
import java.util.List;
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
import static org.zalando.logbook.autoconfigure.webflux.LogbookNettyClientTracingIntegrationTest.callOutbound;
import static org.zalando.logbook.autoconfigure.webflux.LogbookNettyClientTracingIntegrationTest.currentTraceId;

/**
 * The same application as {@link LogbookNettyClientTracingIntegrationTest}, with automatic context propagation
 * turned off. The context is restored from the channel rather than from the reactive chain, so the outgoing entries
 * are expected to carry a trace id here as well.
 *
 * <p>The application's own MDC stays empty in this mode, so there is nothing to compare the entries against; the
 * check is that both of them carry the same trace id and that it is there at all.
 */
@SpringBootTest(
        classes = LogbookNettyClientTracingIntegrationTest.TestApplication.class,
        webEnvironment = RANDOM_PORT,
        properties = {
                "logbook.filter.enabled=false",
                "spring.reactor.context-propagation=limited"
        })
class LogbookNettyClientLimitedPropagationIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private HttpLogWriter writer;

    private final List<String> loggedTraceIds = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        reset(writer);
        loggedTraceIds.clear();
        when(writer.isActive()).thenReturn(true);

        doAnswer(invocation -> loggedTraceIds.add(currentTraceId()))
                .when(writer).write(any(Precorrelation.class), anyString());
        doAnswer(invocation -> loggedTraceIds.add(currentTraceId()))
                .when(writer).write(any(Correlation.class), anyString());
    }

    @Test
    void shouldWriteRequestAndResponseWithATraceIdWithoutAutomaticContextPropagation() throws IOException {
        final String traceIdSeenByTheApplication = callOutbound(port);

        verify(writer, timeout(5_000)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000)).write(any(Correlation.class), anyString());

        assertThat(traceIdSeenByTheApplication)
                .describedAs("the application's own MDC, which this mode leaves empty")
                .isEqualTo(LogbookNettyClientTracingIntegrationTest.NO_TRACE_ID);
        assertThat(loggedTraceIds)
                .describedAs("trace id in the MDC while the outgoing request and its response were written")
                .hasSize(2)
                .doesNotContainNull()
                .satisfies(traceIds -> assertThat(traceIds.get(0)).isNotBlank().isEqualTo(traceIds.get(1)));
    }
}
