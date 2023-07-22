package org.zalando.logbook;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.zalando.logbook.attributes.NoOpRequestAttributesExtractor;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class StrategyTest {

    private final Strategy unit = mock(Strategy.class, InvocationOnMock::callRealMethod);
    private final Precorrelation precorrelation = mock(Precorrelation.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final Correlation correlation = mock(Correlation.class);
    private final HttpResponse response = mock(HttpResponse.class);
    private final Sink sink = mock(Sink.class);

    @Test
    void shouldProcessRequestWithBodyByDefault() throws IOException {
        unit.process(request);

        verify(request).withBody();
    }

    @Test
    void shouldWriteRequestToSinkByDefault() throws IOException {
        unit.write(precorrelation, request, sink);

        verify(sink).write(precorrelation, request);
        verifyNoMoreInteractions(precorrelation, request);
    }

    @Test
    void shouldProcessResponseWithBodyByDefault() throws IOException {
        unit.process(request, response);

        verify(response).withBody();
        verifyNoMoreInteractions(request);
    }

    @Test
    void shouldWriteResponseToSinkByDefault() throws IOException {
        unit.write(correlation, request, response, sink);

        verify(sink).write(correlation, request, response);
        verifyNoMoreInteractions(correlation, request, response);
    }

    @Test
    void shouldReturnNoOpRequestAttributesExtractorByDefault() {
        assertThat(unit.getRequestAttributesExtractor())
                .isInstanceOf(NoOpRequestAttributesExtractor.class);
    }

}
