package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.zalando.logbook.servlet.FormRequestMode.BODY;
import static org.zalando.logbook.servlet.FormRequestMode.OFF;
import static org.zalando.logbook.servlet.FormRequestMode.PARAMETER;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogWriter} correctly.
 */
final class FormRequestTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    @BeforeEach
    void setUp() {
        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldLogNonFormRequest() throws Exception {
        final MockMvc mvc = mvc(FormRequestMode.fromProperties());
        verifyLogsBody(mvc, "application/not-x-www-form-urlencoded", "hello=world");
    }

    @Test
    void shouldNotLogFormRequestOff() throws Exception {
        mvc(OFF).perform(get("/api/sync")
                .with(http11())
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("hello=world"));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String request = captor.getValue();

        assertThat(request, startsWith("Incoming Request:"));
        assertThat(request, containsString("GET http://localhost/api/sync HTTP/1.1"));
        assertThat(request, containsString("Accept: application/json"));
        assertThat(request, containsString("Content-Type: application/x-www-form-urlencoded"));
        assertThat(request, containsString("Host: localhost"));
    }

    @Test
    void shouldLogFormRequestBody() throws Exception {
        verifyLogsBody(mvc(BODY), "application/x-www-form-urlencoded", "hello=Johnson+%26+Johnson");
    }

    @Test
    void shouldLogFormRequestParameter() throws Exception {
        verifyLogsBody(mvc(PARAMETER), "application/x-www-form-urlencoded", "hello=Johnson+%26+Johnson");
    }

    private MockMvc mvc(final FormRequestMode mode) {
        return MockMvcBuilders
                .standaloneSetup(new ExampleController())
                .addFilter(new LogbookFilter(Logbook.builder()
                        .sink(new DefaultSink(formatter, writer))
                        .build()).withFormRequestMode(mode))
                .build();
    }

    private void verifyLogsBody(final MockMvc mvc, final String contentType, final String content) throws Exception {
        mvc.perform(get("/api/sync")
                .with(http11())
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(contentType)
                .content(content));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String request = captor.getValue();

        assertThat(request, startsWith("Incoming Request:"));
        assertThat(request, containsString("GET http://localhost/api/sync HTTP/1.1"));
        assertThat(request, containsString("Accept: application/json"));
        assertThat(request, containsString("Content-Type: " + contentType));
        assertThat(request, containsString("Host: localhost"));
        assertThat(request, containsString(content));
    }

    private RequestPostProcessor http11() {
        return request -> {
            request.setProtocol("HTTP/1.1");
            return request;
        };
    }

}
