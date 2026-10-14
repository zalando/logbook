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
 * The same application and the same check as {@link LogbookNettyClientTracingIntegrationTest}, with incoming logging
 * left on so that both sides are active at once. Every entry, incoming and outgoing alike, is expected to carry the
 * trace id of the call that caused it.
 *
 * <p>The server runs in web filter mode, the mode that logs from inside the reactive chain and so has the trace at
 * hand.
 */
@SpringBootTest(
        classes = LogbookNettyClientTracingIntegrationTest.TestApplication.class,
        webEnvironment = RANDOM_PORT,
        properties = {
                "logbook.reactive.server-mode=web-filter",
                // so the handler method can read the trace id it is meant to be compared against
                "spring.reactor.context-propagation=auto"
        })
class LogbookClientAndServerTracingIntegrationTest {

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
    void shouldWriteEveryEntryWithTheTraceIdOfTheCallThatCausedIt() throws IOException {
        final String callerTraceId = callOutbound(port);

        // the incoming call, the outgoing call it makes, and the incoming call that one arrives as
        verify(writer, timeout(5_000).times(3)).write(any(Precorrelation.class), anyString());
        verify(writer, timeout(5_000).times(3)).write(any(Correlation.class), anyString());

        assertThat(callerTraceId)
                .describedAs("trace id the request handler itself observed")
                .isNotEqualTo(LogbookNettyClientTracingIntegrationTest.NO_TRACE_ID);
        assertThat(loggedTraceIds)
                .describedAs("trace id in the MDC for every entry Logbook wrote")
                .hasSize(6)
                .containsOnly(callerTraceId);
    }
}
