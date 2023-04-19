package org.zalando.logbook.servlet;

import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.api.Correlation;
import org.zalando.logbook.api.HttpLogFormatter;
import org.zalando.logbook.api.HttpLogWriter;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.Logbook;
import org.zalando.logbook.api.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles {@link DispatcherType#ASYNC} correctly.
 */
final class SkipTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ExampleController())
            .addFilter(new LogbookFilter(Logbook.builder()
                    .sink(new DefaultSink(formatter, writer))
                    .build()))
            .build();

    @BeforeEach
    void setUp() {
        reset(formatter, writer);

        when(writer.isActive()).thenReturn(false);
    }


    @Test
    @SuppressWarnings("unchecked")
    void shouldNotLogRequest() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, never()).format(any(), any(HttpRequest.class));
        verify(writer, never()).write(any(Precorrelation.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotLogResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, never()).format(any(), any(HttpResponse.class));
        verify(writer, never()).write(any(Correlation.class), any());
    }

}
