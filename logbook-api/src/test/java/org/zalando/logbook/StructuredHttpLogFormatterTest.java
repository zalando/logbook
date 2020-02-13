package org.zalando.logbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.HttpHeaders.empty;

class StructuredHttpLogFormatterTest {

    private final Precorrelation precorrelation = mock(Precorrelation.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final Correlation correlation = mock(Correlation.class);
    private final HttpResponse response = mock(HttpResponse.class);

    private final StructuredHttpLogFormatter unit = mock(StructuredHttpLogFormatter.class);

    @BeforeEach
    void defaultBehavior() throws IOException {
        when(precorrelation.getId()).thenReturn("469b1d07-e7fc-4854-8595-2db0afcb42e6");

        when(request.getProtocolVersion()).thenReturn("HTTP/1.1");
        when(request.getOrigin()).thenReturn(Origin.REMOTE);
        when(request.getRemote()).thenReturn("127.0.0.1");
        when(request.getHeaders()).thenReturn(empty().update("Test", emptyList()));
        when(request.getContentType()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getScheme()).thenReturn("https");
        when(request.getHost()).thenReturn("www.example.org");
        when(request.getPort()).thenReturn(Optional.empty());
        when(request.getPath()).thenReturn("/search");
        when(request.getQuery()).thenReturn("q=example");
        when(request.getRequestUri()).thenCallRealMethod();
        when(request.getBodyAsString()).thenReturn("");

        when(correlation.getId()).thenReturn("469b1d07-e7fc-4854-8595-2db0afcb42e6");
        when(correlation.getDuration()).thenReturn(Duration.ofMillis(13));

        when(response.getProtocolVersion()).thenReturn("HTTP/1.1");
        when(response.getOrigin()).thenReturn(Origin.REMOTE);
        when(response.getHeaders()).thenReturn(empty().update("Test", emptyList()));
        when(response.getOrigin()).thenReturn(Origin.LOCAL);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentType()).thenReturn(null);
        when(response.getBodyAsString()).thenReturn("");

        when(unit.format(any(Precorrelation.class), any(HttpRequest.class))).thenCallRealMethod();
        when(unit.format(any(Correlation.class), any(HttpResponse.class))).thenCallRealMethod();
        when(unit.prepare(any(Precorrelation.class), any(HttpRequest.class))).thenCallRealMethod();
        when(unit.prepare(any(Correlation.class), any(HttpResponse.class))).thenCallRealMethod();
        when(unit.prepareHeaders(any())).thenCallRealMethod();
        when(unit.prepareBody(any())).thenCallRealMethod();

        when(unit.format(any())).thenAnswer(invocation -> invocation.getArgument(0).toString());
    }

    @Test
    void formatRequest() throws IOException {
        final String format = unit.format(precorrelation, request);
        assertThat(format, containsString("origin=remote"));
    }

    @Test
    void formatResponse() throws IOException {
        final String format = unit.format(correlation, response);
        assertThat(format, containsString("origin=local"));
    }

    @Test
    void prepareRequest() throws IOException {
        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output, hasEntry("origin", "remote"));
        assertThat(output, hasEntry("type", "request"));
        assertThat(output, hasEntry("correlation", "469b1d07-e7fc-4854-8595-2db0afcb42e6"));
        assertThat(output, hasEntry("protocol", "HTTP/1.1"));
        assertThat(output, hasEntry("remote", "127.0.0.1"));
        assertThat(output, hasEntry("method", "GET"));
        assertThat(output, hasEntry("uri", "https://www.example.org/search?q=example"));
        assertThat(output, hasEntry("headers", singletonMap("Test", emptyList())));
        assertThat(output, not(hasKey("body")));
    }

    @Test
    void prepareRequestWithBody() throws IOException {
        when(request.getHeaders()).thenReturn(empty());
        when(request.getBodyAsString()).thenReturn("Hello, world!");

        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output, not(hasKey("headers")));
        assertThat(output, hasEntry("body", "Hello, world!"));
    }

    @Test
    void prepareResponse() throws IOException {
        final Map<String, Object> output = unit.prepare(correlation, response);

        assertThat(output, hasEntry("origin", "local"));
        assertThat(output, hasEntry("type", "response"));
        assertThat(output, hasEntry("correlation", "469b1d07-e7fc-4854-8595-2db0afcb42e6"));
        assertThat(output, hasEntry("protocol", "HTTP/1.1"));
        assertThat(output, hasEntry("status", 200));
        assertThat(output, hasEntry("headers", singletonMap("Test", emptyList())));
        assertThat(output, not(hasKey("body")));
    }

    @Test
    void prepareResponseWithBody() throws IOException {
        when(response.getHeaders()).thenReturn(empty());
        when(response.getBodyAsString()).thenReturn("Hello, world!");

        final Map<String, Object> output = unit.prepare(correlation, response);

        assertThat(output, not(hasKey("headers")));
        assertThat(output, hasEntry("body", "Hello, world!"));
    }

}
