package org.zalando.logbook.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
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

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} handles cases correctly when multiple instances are running in the same chain.
 */
final class MultiFilterTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .sink(new DefaultSink(formatter, writer))
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final ExampleController controller = spy(new ExampleController());

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilter(firstFilter)
            .addFilter(lastFilter)
            .build();

    @BeforeEach
    void setUp() {
        reset(formatter, writer);

        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldFormatRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(), any(HttpRequest.class));
    }

    @Test
    void shouldFormatResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(), any(HttpResponse.class));
    }

    @Test
    void shouldLogRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).write(any(Correlation.class), any());
    }

    @Test
    void shouldBufferRequestTwice() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final RemoteRequest firstRequest = getRequest(lastFilter);
        final RemoteRequest secondRequest = getRequest(controller);

        assertThat(firstRequest.getBody()).isNotEmpty();
        assertThat(secondRequest.getBody()).isNotEmpty();
    }

    @Test
    void shouldBufferResponseTwice() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final LocalResponse firstResponse = getResponse(lastFilter);
        final LocalResponse secondResponse = getResponse(controller);

        assertThat(firstResponse.getBody()).isNotEmpty();
        assertThat(secondResponse.getBody()).isNotEmpty();
    }

    private RemoteRequest getRequest(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<RemoteRequest> captor = ArgumentCaptor.forClass(RemoteRequest.class);
        verify(filter).doFilter(captor.capture(), any(), any());
        return captor.getValue();
    }

    private RemoteRequest getRequest(final ExampleController controller) throws IOException {
        final ArgumentCaptor<RemoteRequest> captor = ArgumentCaptor.forClass(RemoteRequest.class);
        verify(controller).readByte(captor.capture(), any());
        return captor.getValue();
    }

    private LocalResponse getResponse(final Filter filter) throws IOException, ServletException {
        final ArgumentCaptor<LocalResponse> captor = ArgumentCaptor.forClass(LocalResponse.class);
        verify(filter).doFilter(any(), captor.capture(), any());
        return captor.getValue();
    }

    private LocalResponse getResponse(final ExampleController controller) throws IOException {
        final ArgumentCaptor<LocalResponse> captor = ArgumentCaptor.forClass(LocalResponse.class);
        verify(controller).readBytes(any(), captor.capture());
        return captor.getValue();
    }

}
