package org.zalando.logbook.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BodyOnlyIfStatusAtLeastStrategyTest {

    private final Sink sink = mock(Sink.class);

    private final Logbook unit = Logbook.builder()
            .strategy(new BodyOnlyIfStatusAtLeastStrategy(400))
            .sink(sink)
            .build();

    private final MockHttpRequest request = MockHttpRequest.create().withBodyAsString("Hello");
    private final MockHttpResponse response = MockHttpResponse.create().withBodyAsString("World");

    @BeforeEach
    void defaultBehaviour() {
        when(sink.isActive()).thenReturn(true);
    }

    @Test
    void shouldDeferWritingOfRequest() throws IOException {
        unit.process(request).write();

        verify(sink, never()).write(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 301})
    void shouldNotWriteAnythingWhenSuccessful(final int status) throws IOException {
        unit.process(request).write().process(response.withStatus(status)).write();

        final ArgumentCaptor<HttpRequest> writtenRequest = ArgumentCaptor.forClass(HttpRequest.class);
        final ArgumentCaptor<HttpResponse> writtenResponse = ArgumentCaptor.forClass(HttpResponse.class);

        verify(sink).writeBoth(any(), writtenRequest.capture(), writtenResponse.capture());

        assertThat(writtenRequest.getValue().getBodyAsString()).isEmpty();
        assertThat(writtenResponse.getValue().getBodyAsString()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 500, 503})
    void shouldLogBothWhenError(final int status) throws IOException {
        unit.process(request).write().process(response.withStatus(status)).write();

        final ArgumentCaptor<HttpRequest> writtenRequest = ArgumentCaptor.forClass(HttpRequest.class);
        final ArgumentCaptor<HttpResponse> writtenResponse = ArgumentCaptor.forClass(HttpResponse.class);

        verify(sink).writeBoth(any(), writtenRequest.capture(), writtenResponse.capture());

        assertThat(writtenRequest.getValue().getBodyAsString()).isEqualTo("Hello");
        assertThat(writtenResponse.getValue().getBodyAsString()).isEqualTo("World");
    }

}
