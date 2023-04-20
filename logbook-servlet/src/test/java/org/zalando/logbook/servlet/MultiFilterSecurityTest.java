package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.Conditions.requestTo;
import static org.zalando.logbook.servlet.RequestBuilders.async;

/**
 * Verifies that {@link LogbookFilter} handles complex security setups correctly.
 */
final class MultiFilterSecurityTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);
    private final SecurityFilter securityFilter = spy(new SecurityFilter());

    private final Logbook logbook = Logbook.builder()
            .condition(exclude(requestTo("/api/empty")))
            .sink(new DefaultSink(formatter, writer))
            .build();

    private final ExampleController controller = spy(new ExampleController());

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
            .addFilter(spy(new SpyableFilter(new SecureLogbookFilter(logbook))))
            .addFilter(securityFilter)
            .addFilter(spy(new SpyableFilter(new LogbookFilter(logbook))))
            .build();

    @BeforeEach
    void setUp() {
        reset(formatter, writer);

        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldFormatAuthorizedRequestOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(), any(HttpRequest.class));
    }

    @Test
    void shouldFormatAuthorizedResponseOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(), any(HttpResponse.class));
    }

    @Test
    void shouldLogAuthorizedRequestOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer).write(any(Precorrelation.class), any());
    }

    @Test
    void shouldLogAuthorizedResponseOnce() throws Exception {
        mvc.perform(get("/api/sync"));

        verify(writer).write(any(Precorrelation.class), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldFormatUnauthorizedRequestOnce(final int status) throws Exception {
        securityFilter.setStatus(status);

        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(), any(HttpRequest.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldFormatUnauthorizedResponseOnce(final int status) throws Exception {
        securityFilter.setStatus(status);

        mvc.perform(get("/api/sync"));

        verify(formatter).format(any(), any(HttpResponse.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldLogUnauthorizedRequestOnce(final int status) throws Exception {
        securityFilter.setStatus(status);

        mvc.perform(get("/api/sync"));

        verify(writer).write(any(Precorrelation.class), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldLogUnauthorizedResponseOnce(final int status) throws Exception {
        securityFilter.setStatus(status);

        mvc.perform(get("/api/sync"));

        verify(writer).write(any(Precorrelation.class), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldNotLogRequestBodyForUnauthorizedRequests(final int status) throws Exception {
        securityFilter.setStatus(status);

        mvc.perform(post("/api/sync")
                .content("Hello, world!"));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String request = captor.getValue();

        assertThat(request).doesNotContain("Hello, world");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void shouldNotLogUnauthorizedRequest(final int status) throws Exception {
        when(writer.isActive()).thenReturn(false);
        securityFilter.setStatus(status);

        mvc.perform(get("/api/sync"));

        verify(writer, never()).write(any(Precorrelation.class), any());
        verify(writer, never()).write(any(Correlation.class), any());
    }

    @Test
    void shouldHandleUnauthorizedAsyncDispatchRequest() throws Exception {
        mvc.perform(async(mvc.perform(get("/api/unauthorized"))
                .andExpect(request().asyncStarted())
                .andReturn()));
    }

    @Test
    void shouldEcho() throws Exception {
        mvc.perform(post("/api/echo").content("Hello, world!"));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        assertThat(captor.getValue()).contains("Hello, world!");
    }

    @Test
    void shouldNotLogEmpty() throws Exception {
        mvc.perform(get("/api/empty")).andExpect(content().string(""));

        verify(writer, never()).write(any(Precorrelation.class), anyString());
    }

}
