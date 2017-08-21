package org.zalando.logbook.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.logbook.BaseHttpMessage;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Precorrelation;

import java.io.IOException;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
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
public final class FormattingTest {

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
    void shouldFormatRequest() throws Exception {
        mvc.perform(get("/api/sync?limit=1")
                .accept(MediaType.TEXT_PLAIN));

        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("remote address", HttpRequest::getRemote, is("127.0.0.1")));
        assertThat(request, hasFeature("method", HttpRequest::getMethod, is("GET")));
        assertThat(request, hasFeature("url", HttpRequest::getRequestUri,
                hasToString("http://localhost/api/sync?limit=1")));
        assertThat(request, hasFeature("query", HttpRequest::getQuery, is("limit=1")));
        assertThat(request, hasFeature("headers", HttpRequest::getHeaders,
                is(singletonMap("Accept", singletonList("text/plain")))));
        assertThat(request, hasFeature("body", this::getBody, is(notNullValue())));
        assertThat(request, hasFeature("body", this::getBodyAsString, is(emptyString())));
    }

    @Test
    void shouldFormatPostParameterRequest() throws Exception {
        mvc.perform(post("/api/sync")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=Alice&age=7.5"));


        final HttpRequest request = interceptRequest();

        assertThat(request, hasFeature("method", HttpRequest::getMethod, is("POST")));
        assertThat(request, hasFeature("body", this::getBodyAsString, is("name=Alice&age=7.5")));
    }

    @Test
    void shouldFormatResponse() throws Exception {
        mvc.perform(get("/api/sync"));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
        assertThat(response, hasFeature("headers", BaseHttpMessage::getHeaders,
                hasEntry("Content-Type", singletonList("application/json;charset=UTF-8"))));
        assertThat(response, hasFeature("content type",
                HttpResponse::getContentType, is("application/json;charset=UTF-8")));

        with(response.getBodyAsString())
                .assertThat("$.*", hasSize(1))
                .assertThat("$.value", is("Hello, world!"));
    }

    @Test
    void shouldFormatResponseWithoutBody() throws Exception {
        mvc.perform(get("/api/empty"));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
        assertThat(response, hasFeature("body", this::getBody, is(notNullValue())));
        assertThat(response, hasFeature("body", this::getBodyAsString, is(emptyString())));
    }

    @Test
    void shouldFormatResponseWithBinaryBody() throws Exception {
        mvc.perform(post("/api/binary"));

        final HttpResponse response = interceptResponse();

        assertThat(response, hasFeature("status", HttpResponse::getStatus, is(200)));
        assertThat(response, hasFeature("body", this::getBody, is(notNullValue())));
        assertThat(response, hasFeature("body", this::getBodyAsString, is("<binary>")));
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
        @SuppressWarnings("unchecked") final ArgumentCaptor<Precorrelation<HttpRequest>> captor = ArgumentCaptor.forClass(
                Precorrelation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getRequest();
    }

    private HttpResponse interceptResponse() throws IOException {
        @SuppressWarnings("unchecked") final ArgumentCaptor<Correlation<HttpRequest, HttpResponse>> captor = ArgumentCaptor.forClass(
                Correlation.class);
        verify(formatter).format(captor.capture());
        return captor.getValue().getResponse();
    }

}
