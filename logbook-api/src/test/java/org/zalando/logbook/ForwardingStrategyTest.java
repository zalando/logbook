package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ForwardingStrategyTest {

    private final Strategy strategy = mock(Strategy.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);
    private final Sink sink = mock(Sink.class);
    private final Precorrelation precorrelation = mock(Precorrelation.class);
    private final Correlation correlation = mock(Correlation.class);

    private final ForwardingStrategy unit = () -> strategy;

    @Test
    void process() throws IOException {
        unit.process(request);
        verify(strategy).process(request);

        unit.write(precorrelation, request, sink);
        verify(strategy).write(precorrelation, request, sink);

        unit.process(request, response);
        verify(strategy).process(request, response);

        unit.write(correlation, request, response, sink);
        verify(strategy).write(correlation, request, response, sink);

        unit.getRequestAttributesExtractor();
        verify(strategy).getRequestAttributesExtractor();
    }
}
