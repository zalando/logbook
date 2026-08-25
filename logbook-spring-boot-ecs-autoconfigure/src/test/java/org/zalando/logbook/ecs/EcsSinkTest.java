package org.zalando.logbook.ecs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EcsSinkTest {

    @Mock
    private EcsStructuredHttpLogFormatter ecsStructuredHttpLogFormatter;

    @Mock
    private Precorrelation precorrelation;

    @Mock
    private Correlation correlation;

    private final MockHttpRequest request = MockHttpRequest.create();

    private static final Logger LOGGER = LoggerFactory.getLogger(Logbook.class);

    @Test
    void writeRequestDelegatesToTraceWriter() throws IOException {
        EcsSink unit = spy(new EcsSink(ecsStructuredHttpLogFormatter));
        final Map<String, Object> mockRequestContent = Map.of("test", "test");

        when(ecsStructuredHttpLogFormatter.prepare(precorrelation, request)).thenReturn(mockRequestContent);

        unit.write(precorrelation, request);

        verify(ecsStructuredHttpLogFormatter).prepare(precorrelation, request);
        verify(unit).write(mockRequestContent, LOGGER.atError());
    }


    @ParameterizedTest
    @ValueSource(ints = {100, 200, 300, 400, 500})
    void writeResponseUsingTraceWriter(int status) throws IOException {
        EcsSink unit = spy(new EcsSink(ecsStructuredHttpLogFormatter));
        final MockHttpResponse response = MockHttpResponse.create().withStatus(status);
        final Map<String, Object> mockResponseContent = Map.of("test", "test");

        when(ecsStructuredHttpLogFormatter.prepare(correlation, response)).thenReturn(mockResponseContent);

        unit.write(correlation, request, response);

        verify(ecsStructuredHttpLogFormatter).prepare(correlation, response);
        verify(unit).write(mockResponseContent, LOGGER.atTrace());

    }
}
