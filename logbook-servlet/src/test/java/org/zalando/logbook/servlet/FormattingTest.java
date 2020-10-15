package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.TestStrategy;

import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Verifies that {@link LogbookFilter} delegates to {@link HttpLogFormatter} correctly.
 */
final class FormattingTest {

    private final HttpLogFormatter formatter = spy(new ForwardingHttpLogFormatter(new DefaultHttpLogFormatter()));
    private final HttpLogWriter writer = mock(HttpLogWriter.class);

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ExampleController())
            .addFilter(new LogbookFilter(Logbook.builder()
                    .strategy(new TestStrategy())
                    .sink(new DefaultSink(formatter, writer))
                    .build()))
            .build();

    @BeforeEach
    void setUp() {
        reset(formatter, writer);

        when(writer.isActive()).thenReturn(true);
    }

    @Test
    void shouldFormatRequest() throws Exception {
        mvc.perform(get("/api/sync?limit=1")
                .accept(MediaType.TEXT_PLAIN));

        final HttpRequest request = interceptRequest();

        assertThat(request.getRemote()).isEqualTo("127.0.0.1");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getRequestUri()).hasToString("http://localhost/api/sync?limit=1");
        assertThat(request.getQuery()).isEqualTo("limit=1");
        assertThat(request.getHeaders()).containsOnly(entry("Accept", singletonList("text/plain")));
        assertThat(getBody(request)).isNotNull();
        assertThat(getBodyAsString(request)).isEmpty();
    }

    @Test
    void shouldFormatPostParameterRequest() throws Exception {
        mvc.perform(post("/api/sync")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=Alice&age=7.5"));


        final HttpRequest request = interceptRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(getBodyAsString(request)).isEqualTo("name=Alice&age=7.5");
    }

    @Test
    void shouldFormatResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        final HttpResponse response = interceptResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaders()).hasEntrySatisfying(
                "Content-Type",
                values -> assertThat(values).satisfies(value ->
                                assertThat(value).startsWith("application/json"), atIndex(0)));
        assertThat(response.getContentType()).startsWith("application/json");

        with(response.getBodyAsString())
                .assertEquals("$", singletonMap("value", "Hello, world!"));
    }

    @Test
    void shouldFormatResponseWithoutBody() throws Exception {
        mvc.perform(get("/api/empty"));

        final HttpResponse response = interceptResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(getBody(response)).isNotNull();
        assertThat(getBodyAsString(response)).isEmpty();
    }

    @Test
    void shouldFormatResponseWithBinaryBody() throws Exception {
        mvc.perform(post("/api/binary"));

        final HttpResponse response = interceptResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(getBody(response)).isNotNull();
        assertThat(getBodyAsString(response)).isEqualTo("<binary>");
    }

    @Test
    void shouldIgnoreBodies() throws Exception {
        mvc.perform(get("/api/sync")
                .header("Ignore", true)
                .accept(MediaType.TEXT_PLAIN));

        final HttpRequest request = interceptRequest();
        assertEquals("", request.getBodyAsString());

        final HttpResponse response = interceptResponse();
        assertEquals("", response.getBodyAsString());
    }

    private byte[] getBody(final HttpMessage message) {
        try {
            return message.getBody();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private String getBodyAsString(final HttpMessage message) {
        try {
            return message.getBodyAsString();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private HttpRequest interceptRequest() throws IOException {
        final ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(formatter).format(any(), captor.capture());
        return captor.getValue();
    }

    private HttpResponse interceptResponse() throws IOException {
        final ArgumentCaptor<HttpResponse> captor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(formatter).format(any(), captor.capture());
        return captor.getValue();
    }

}
