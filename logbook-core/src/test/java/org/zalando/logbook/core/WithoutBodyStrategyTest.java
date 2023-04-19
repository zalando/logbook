package org.zalando.logbook.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Sink;
import org.zalando.logbook.test.MockHttpRequest;
import org.zalando.logbook.test.MockHttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WithoutBodyStrategyTest {

    private final Sink sink = mock(Sink.class);

    private final Logbook unit = Logbook.builder()
            .strategy(new WithoutBodyStrategy())
            .sink(sink)
            .build();

    private final MockHttpRequest request = MockHttpRequest.create().withBodyAsString("Hello");
    private final MockHttpResponse response = MockHttpResponse.create().withBodyAsString("World");

    @BeforeEach
    void defaultBehaviour() {
        when(sink.isActive()).thenReturn(true);
    }

    @Test
    void shouldWriteRequestWithoutBody() throws IOException {
        unit.process(request).write();

        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(sink).write(any(), captor.capture());
        assertThat(captor.getValue().getBodyAsString()).isEmpty();
    }

    @Test
    void shouldWriteResponseWithoutBody() throws IOException {
        unit.process(request).write().process(response).write();

        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(sink).write(any(), any(), captor.capture());
        assertThat(captor.getValue().getBodyAsString()).isEmpty();
    }

}
