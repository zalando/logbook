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
class HttpStatusCodeBasedEcsSinkTest {

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
    void setupLoggingEventBuilder() {
        when(ecsStructuredHttpLogFormatter.format(CONTENT)).thenReturn(CONTENT.toString());
    }

    @Test
    void writeRequestDelegatesToTraceWriter() throws IOException {
        final HttpStatusCodeBasedEcsSink unit = new HttpStatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);

        when(ecsStructuredHttpLogFormatter.prepare(precorrelation, request)).thenReturn(CONTENT);
        when(logger.atTrace()).thenReturn(loggingEventBuilder);

        unit.write(precorrelation, request);

        verify(ecsStructuredHttpLogFormatter).prepare(precorrelation, request);
        verify(logger).atTrace();
        verify(loggingEventBuilder).addKeyValue(KEY, VALUE);
        verify(loggingEventBuilder).log(CONTENT.toString());
    }


    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 399})
    void write1xxTo3xxResponseStatusDelegatesToTraceWriter(int status) throws IOException {
        final HttpStatusCodeBasedEcsSink unit = new HttpStatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(logger.atTrace()).thenReturn(loggingEventBuilder);
        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atTrace();
        verify(logger, never()).atWarn();
        verify(logger, never()).atError();
        verify(loggingEventBuilder).log(CONTENT.toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 499})
    void write4xxTo499ResponseStatusDelegatesToWarnWriter(int status) throws IOException {
        final HttpStatusCodeBasedEcsSink unit = new HttpStatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(logger.atWarn()).thenReturn(loggingEventBuilder);
        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atWarn();
        verify(logger, never()).atTrace();
        verify(logger, never()).atError();
        verify(loggingEventBuilder).log(CONTENT.toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 600, 700})
    void write5xxAndHigherResponseStatusDelegatesToErrorWriter(int status) throws IOException {
        final HttpStatusCodeBasedEcsSink unit = new HttpStatusCodeBasedEcsSink(logger, ecsStructuredHttpLogFormatter);
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);

        when(logger.atError()).thenReturn(loggingEventBuilder);
        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(CONTENT);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(logger).atError();
        verify(logger, never()).atTrace();
        verify(logger, never()).atWarn();
        verify(loggingEventBuilder).log(CONTENT.toString());
    }
}