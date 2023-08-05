package org.zalando.logbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.attributes.HttpAttributes;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(request.getHeaders()).thenReturn(HttpHeaders.empty().update("Test", emptyList()));
        when(request.getContentType()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getScheme()).thenReturn("https");
        when(request.getHost()).thenReturn("www.example.org");
        when(request.getPort()).thenReturn(Optional.empty());
        when(request.getPath()).thenReturn("/search");
        when(request.getQuery()).thenReturn("q=example");
        when(request.getRequestUri()).thenCallRealMethod();
        when(request.getBodyAsString()).thenReturn("");

        when(request.getAttributes()).thenCallRealMethod();
        when(response.getAttributes()).thenCallRealMethod();

        when(correlation.getId()).thenReturn("469b1d07-e7fc-4854-8595-2db0afcb42e6");
        when(correlation.getDuration()).thenReturn(Duration.ofMillis(13));

        when(response.getProtocolVersion()).thenReturn("HTTP/1.1");
        when(response.getOrigin()).thenReturn(Origin.REMOTE);
        when(response.getHeaders()).thenReturn(HttpHeaders.empty().update("Test", emptyList()));
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
        when(unit.preparePort(any())).thenCallRealMethod();

        when(unit.format(any())).thenAnswer(invocation -> invocation.getArgument(0).toString());
    }

    @Test
    void formatRequest() throws IOException {
        final String format = unit.format(precorrelation, request);
        assertThat(format).contains("origin=remote");
    }

    @Test
    void formatResponse() throws IOException {
        final String format = unit.format(correlation, response);
        assertThat(format).contains("origin=local");
    }

    @Test
    void prepareRequest() throws IOException {
        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output)
                .containsEntry("origin", "remote")
                .containsEntry("type", "request")
                .containsEntry("correlation", "469b1d07-e7fc-4854-8595-2db0afcb42e6")
                .containsEntry("protocol", "HTTP/1.1")
                .containsEntry("remote", "127.0.0.1")
                .containsEntry("method", "GET")
                .containsEntry("uri", "https://www.example.org/search?q=example")
                .containsEntry("headers", singletonMap("Test", emptyList()))
                .containsEntry("host", "www.example.org")
                .containsEntry("path", "/search")
                .containsEntry("scheme", "https")
                .containsEntry("port", null)
                .doesNotContainKey("body");
    }

    @Test
    void prepareRequestWithPort() throws IOException {
        when(request.getPort()).thenReturn(Optional.of(8080));

        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output)
                .containsEntry("port", "8080");
    }

    @Test
    void prepareRequestWithBody() throws IOException {
        when(request.getHeaders()).thenReturn(HttpHeaders.empty());
        when(request.getBodyAsString()).thenReturn("Hello, world!");

        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output)
                .doesNotContainKey("headers")
                .containsEntry("body", "Hello, world!");
    }

    @Test
    void prepareRequestWithoutHttpAttributes() throws IOException {
        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output).doesNotContainKey("attributes");
    }

    @Test
    void prepareRequestWithHttpAttributes() throws IOException {
        final HttpAttributes attributes = HttpAttributes.of("key", "val");
        when(request.getAttributes()).thenReturn(attributes);

        final Map<String, Object> output = unit.prepare(precorrelation, request);

        assertThat(output).containsEntry("attributes", attributes);
    }

    @Test
    void prepareResponseWithHttpAttributes() throws IOException {
        final HttpAttributes attributes = HttpAttributes.of("key", "val");
        when(response.getAttributes()).thenReturn(attributes);

        final Map<String, Object> output = unit.prepare(correlation, response);

        assertThat(output).containsEntry("attributes", attributes);
    }

    @Test
    void prepareResponse() throws IOException {
        final Map<String, Object> output = unit.prepare(correlation, response);

        assertThat(output)
                .containsEntry("origin", "local")
                .containsEntry("type", "response")
                .containsEntry("correlation", "469b1d07-e7fc-4854-8595-2db0afcb42e6")
                .containsEntry("protocol", "HTTP/1.1")
                .containsEntry("status", 200)
                .containsEntry("headers", singletonMap("Test", emptyList()))
                .doesNotContainKey("body");
    }

    @Test
    void prepareResponseWithBody() throws IOException {
        when(response.getHeaders()).thenReturn(HttpHeaders.empty());
        when(response.getBodyAsString()).thenReturn("Hello, world!");

        final Map<String, Object> output = unit.prepare(correlation, response);

        assertThat(output)
                .doesNotContainKey("headers")
                .containsEntry("body", "Hello, world!");
    }

}
