package org.zalando.logbook.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultEcsSinkTest {

    @Mock
    private EcsStructuredHttpLogFormatter ecsStructuredHttpLogFormatter;

    @Mock
    private Precorrelation precorrelation;

    @Mock
    private Correlation correlation;

    @Mock
    private Logger logger;

    @Mock
    private LoggingEventBuilder loggingEventBuilder;

    private final MockHttpRequest request = MockHttpRequest.create();
    private static final String KEY = "http.version", VALUE = "1.1";
    private static final Map<String, Object> CONTENT = Map.of(KEY, VALUE);

    @BeforeEach
    void setup() {
        when(ecsStructuredHttpLogFormatter.format(CONTENT)).thenReturn(CONTENT.toString());
        when(logger.atTrace()).thenReturn(loggingEventBuilder);
    }

    @Test
    void writeRequestDelegatesToTraceWriter() throws IOException {
        final DefaultEcsSink unit = new DefaultEcsSink(logger, ecsStructuredHttpLogFormatter);

        when(ecsStructuredHttpLogFormatter.prepare(precorrelation, request)).thenReturn(CONTENT);

        unit.write(precorrelation, request);

        verify(ecsStructuredHttpLogFormatter).prepare(precorrelation, request);
        verifyLogsContentAtTrace();
    }


    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 400, 500})
    void writeResponseUsingTraceWriter(int status) throws IOException {
        final DefaultEcsSink unit = new DefaultEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verifyLogsContentAtTrace();
    }

    void verifyLogsContentAtTrace() {
        verify(logger).atTrace();
        verify(logger, never()).atWarn();
        verify(logger, never()).atError();
        verify(loggingEventBuilder).addKeyValue(KEY, VALUE);
        verify(loggingEventBuilder).log(CONTENT.toString());
    }
}