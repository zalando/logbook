package org.zalando.logbook.ecs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class StatusCodeBasedEcsSinkTest {

    @Mock
    private EcsStructuredHttpLogFormatter ecsStructuredHttpLogFormatter;

    @Mock
    private Precorrelation precorrelation;

    @Mock
    private Correlation correlation;

    @Spy
    private Logger logger;

    private final MockHttpRequest request = MockHttpRequest.create();
    private static final Map<String, Object> CONTENT = Map.of("test", "test");

    @Test
    void writeRequestDelegatesToTraceWriter() throws IOException {
        final StatusCodeBasedEcsSink unit = new StatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        when(ecsStructuredHttpLogFormatter.prepare(precorrelation, request)).thenReturn(CONTENT);

        unit.write(precorrelation, request);

        verify(ecsStructuredHttpLogFormatter).prepare(precorrelation, request);
        verify(logger).atTrace();
    }


    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 399})
    void write1xxTo3xxResponseStatusDelegatesToTraceWriter(int status) throws IOException {
        final StatusCodeBasedEcsSink unit = new StatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atTrace();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 499})
    void write4xxTo499ResponseStatusDelegatesToWarnWriter(int status) throws IOException {
        final StatusCodeBasedEcsSink unit = new StatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atWarn();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 600, 700})
    void write5xxAndHigherResponseStatusDelegatesToErrorWriter(int status) throws IOException {
        final StatusCodeBasedEcsSink unit = new StatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atError();
    }
}
