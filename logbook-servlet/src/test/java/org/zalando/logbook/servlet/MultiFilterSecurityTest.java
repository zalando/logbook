package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.JsonHttpLogFormatter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.zalando.logbook.servlet.RequestBuilders.async;

/**
 * Verifies that {@link LogbookFilter} handles complex security setups correctly.
 */
public final class MultiFilterSecurityTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new JsonHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final SecurityFilter securityFilter = spy(new SecurityFilter());

    private final Logbook logbook = Logbook.builder()
            .formatter(formatter)
            .writer(writer)
            .build();

    private final Filter firstFilter = spy(new SpyableFilter(new LogbookFilter(logbook, Strategy.SECURITY)));
    private final Filter lastFilter = spy(new SpyableFilter(new LogbookFilter(logbook)));
    private final ExampleController controller = spy(new ExampleController());

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilter(firstFilter)
            .addFilter(securityFilter)
            .addFilter(lastFilter)
            .build();

    @BeforeEach
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatAuthorizedRequestOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatAuthorizedResponseOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(Correlation.class));
    }

    @Test
    void shouldLogAuthorizedRequestOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer).writeRequest(any());
    }

    @Test
    void shouldLogAuthorizedResponseOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer).writeResponse(any());
    }

    @Test
    void shouldBufferAuthorizedRequestOnlyOnce() throws Exception {
        mvc.perform(get("/api/read-byte")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final RemoteRequest firstRequest = getRequest(securityFilter);
        final RemoteRequest secondRequest = getRequest(controller);

        assertNull(firstRequest.getBody());
        assertThat(secondRequest.getBody().length, is(greaterThan(0)));
    }

    @Test
    void shouldBufferAuthorizedResponseTwice() throws Exception {
        mvc.perform(get("/api/read-bytes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!")).andReturn();

        final LocalResponse firstResponse = getResponse(securityFilter);
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

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatUnauthorizedRequestOnce() throws Exception {
        securityFilter.setStatus(401);

        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(Precorrelation.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFormatUnauthorizedResponseOnce() throws Exception {
        securityFilter.setStatus(401);

        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(Correlation.class));
    }

    @Test
    void shouldLogUnauthorizedRequestOnce() throws Exception {
        securityFilter.setStatus(401);

        mvc.perform(get("/api/sync"));

        verify(writer).writeRequest(any());
    }

    @Test
    void shouldLogUnauthorizedResponseOnce() throws Exception {
        securityFilter.setStatus(401);

        mvc.perform(get("/api/sync"));

        verify(writer).writeResponse(any());
    }

    @Test
    void shouldNotLogRequestBodyForUnauthorizedRequests() throws Exception {
        securityFilter.setStatus(401);

        mvc.perform(post("/api/sync")
                .content("Hello, world!"));

        @SuppressWarnings("unchecked") final ArgumentCaptor<Precorrelation<String>> captor = ArgumentCaptor.forClass(
                Precorrelation.class);
        verify(writer).writeRequest(captor.capture());
        final Precorrelation<String> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), not(containsString("Hello, world")));
    }

    @Test
    void shouldNotLogUnauthorizedRequest() throws Exception {
        when(writer.isActive(any())).thenReturn(false);
        securityFilter.setStatus(401);

        mvc.perform(get("/api/sync"));

        verify(writer, never()).writeRequest(any());
        verify(writer, never()).writeResponse(any());
    }

    @Test
    void shouldHandleUnauthorizedAsyncDispatchRequest() throws Exception {
        mvc.perform(async(mvc.perform(get("/api/unauthorized"))
                .andExpect(request().asyncStarted())
                .andReturn()));
    }

}
