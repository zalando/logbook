package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
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
public final class MultiFilterTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final Logbook logbook = Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final ExampleController controller = spy(new ExampleController());

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilter(firstFilter)
            .addFilter(lastFilter)
            .build();

    @BeforeEach
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter, times(2)).format(any(Correlation.class));
    }

    @Test
    void shouldLogRequestTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).writeRequest(any());
    }

    @Test
    void shouldLogResponseTwice() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer, times(2)).writeResponse(any());
    }

    @Test
    void shouldBufferRequestTwice() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final RemoteRequest firstRequest = getRequest(lastFilter);
        final RemoteRequest secondRequest = getRequest(controller);

        assertThat(firstRequest.getBody().length, is(greaterThan(0)));
        assertThat(secondRequest.getBody().length, is(greaterThan(0)));
    }

    @Test
    void shouldBufferResponseTwice() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final LocalResponse firstResponse = getResponse(lastFilter);
        final LocalResponse secondResponse = getResponse(controller);

        assertThat(firstResponse.getBody().length, is(greaterThan(0)));
        assertThat(secondResponse.getBody().length, is(greaterThan(0)));
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
