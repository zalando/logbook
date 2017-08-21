package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogWriter} correctly.
 */
public final class WritingTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ExampleController())
            .addFilter(new LogbookFilter(Logbook.builder()
                    .formatter(formatter)
                    .writer(writer)
                    .build()))
            .build();

    @BeforeEach
    public void setUp() throws IOException {
        reset(formatter, writer);

        when(writer.isActive(any())).thenReturn(true);
    }

    @Test
    void shouldLogRequest() throws Exception {
        mvc.perform(get("/api/sync")
                .with(protocol("HTTP/1.1"))
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello, world!"));

        @SuppressWarnings("unchecked") final ArgumentCaptor<Precorrelation<String>> captor = ArgumentCaptor.forClass(
                Precorrelation.class);
        verify(writer).writeRequest(captor.capture());
        final Precorrelation<String> precorrelation = captor.getValue();

        assertThat(precorrelation.getRequest(), startsWith("Incoming Request:"));
        assertThat(precorrelation.getRequest(), endsWith(
                "GET http://localhost/api/sync HTTP/1.1\n" +
                        "Accept: application/json\n" +
                        "Host: localhost\n" +
                        "Content-Type: text/plain\n" +
                        "\n" +
                        "Hello, world!"));
    }

    @Test
    void shouldLogResponse() throws Exception {
        mvc.perform(get("/api/sync")
                .with(protocol("HTTP/1.1")));

        @SuppressWarnings("unchecked") final ArgumentCaptor<Correlation<String, String>> captor = ArgumentCaptor.forClass(
                Correlation.class);
        verify(writer).writeResponse(captor.capture());
        final Correlation<String, String> correlation = captor.getValue();

        assertThat(correlation.getResponse(), startsWith("Outgoing Response:"));
        assertThat(correlation.getResponse(), endsWith(
                "HTTP/1.1 200 OK\n" +
                        "Content-Type: application/json;charset=UTF-8\n" +
                        "\n" +
                        "{\"value\":\"Hello, world!\"}"));
    }

    private RequestPostProcessor protocol(final String protocol) {
        return request -> {
            request.setProtocol(protocol);
            return request;
        };
    }

}
