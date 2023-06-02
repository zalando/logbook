package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.core.DefaultHttpLogFormatter;
import org.zalando.logbook.core.DefaultSink;

import javax.annotation.concurrent.NotThreadSafe;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogWriter} correctly.
 */
@NotThreadSafe
final class WritingTest {

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

        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldLogRequest() throws Exception {
        mvc.perform(get("/api/sync")
                .with(http11())
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType("text/plain")
                .content("Hello, world!"));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        final String request = captor.getValue();

        assertThat(request)
                .startsWith("Incoming Request:")
                .contains(
                        "GET http://localhost/api/sync HTTP/1.1",
                        "Accept: application/json",
                        "Content-Type: " + "text/plain",
                        "Host: localhost",
                        "Hello, world!");
    }

    @Test
    void shouldLogResponse() throws Exception {
        mvc.perform(get("/api/sync")
                .with(http11()));

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        final String response = captor.getValue();

        assertThat(response)
                .startsWith("Outgoing Response:")
                .contains("HTTP/1.1 200 OK", "Content-Type: application/json")
                .endsWith("\n\n{\"value\":\"Hello, world!\"}");
    }

    @Test
    void shouldLogJsonRequestWithNonAsciiCharacters() throws Exception {
        mvc.perform(post("/api/echo")
                .content("喝点咖啡")
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON)
        );

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        assertThat(captor.getValue()).contains("喝点咖啡");
    }

    @Test
    void shouldLogJsonResponseWithNonAsciiCharacters() throws Exception {
        mvc.perform(post("/api/echo")
                .content("喝点咖啡")
                .characterEncoding("UTF-8")
                .with(http11())
                .accept(MediaType.APPLICATION_JSON)
                .header("Host", "localhost")
                .contentType(MediaType.APPLICATION_JSON)
        );

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Correlation.class), captor.capture());
        assertThat(captor.getValue()).contains("喝点咖啡");
    }

    @Test
    void shouldLogJsonRequestUsingTheProvidedEncoding() throws Exception {
        mvc.perform(post("/api/echo")
                .content("喝点咖啡".getBytes(StandardCharsets.UTF_16))
                .contentType("application/json;charset=UTF-16")
        );

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(any(Precorrelation.class), captor.capture());
        assertThat(captor.getValue()).contains("喝点咖啡");
    }


    private RequestPostProcessor http11() {
        return request -> {
            request.setProtocol("HTTP/1.1");
            return request;
        };
    }

}
