package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.ForwardingHttpRequestTest.mockHttpRequest;
import static org.zalando.logbook.Origin.REMOTE;

public final class ForwardingRawHttpRequestTest {

    private final RawHttpRequest unit = new ForwardingRawHttpRequest() {

        @Override
        protected RawHttpRequest delegate() {
            final RawHttpRequest request = mock(RawHttpRequest.class);

            when(request.getOrigin()).thenReturn(REMOTE);
            when(request.getRemote()).thenReturn("127.0.0.1");
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestUri()).thenReturn("http://localhost/");
            when(request.getScheme()).thenReturn("http");
            when(request.getHost()).thenReturn("localhost");
            when(request.getPort()).thenReturn(Optional.of(8080));
            when(request.getPath()).thenReturn("/");
            when(request.getQuery()).thenReturn("");
            when(request.getProtocolVersion()).thenReturn("HTTP/1.1");
            when(request.getHeaders()).thenReturn(emptyMap());
            when(request.getContentType()).thenReturn("");
            when(request.getCharset()).thenReturn(UTF_8);

            try {
                final HttpRequest mock = mockHttpRequest();
                when(request.withBody()).thenReturn(mock);
            } catch (final IOException e) {
                throw new AssertionError(e);
            }

            return request;
        }

    };

    @Test
    public void shouldDelegate() throws IOException {
        assertThat(unit.getOrigin(), is(REMOTE));
        assertThat(unit.getRemote(), is("127.0.0.1"));
        assertThat(unit.getMethod(), is("GET"));
        assertThat(unit.getRequestUri(), is("http://localhost/"));
        assertThat(unit.getScheme(), is("http"));
        assertThat(unit.getHost(), is("localhost"));
        assertThat(unit.getPort(), is(Optional.of(8080)));
        assertThat(unit.getPath(), is("/"));
        assertThat(unit.getQuery(), is(emptyString()));
        assertThat(unit.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(unit.getHeaders(), is(emptyMap()));
        assertThat(unit.getContentType(), is(""));
        assertThat(unit.getCharset(), is(UTF_8));
        assertNotNull(unit.toString());
    }

    @Test
    public void shouldDelegateWithBody() throws IOException {
        final HttpRequest request = unit.withBody();

        assertThat(request.getOrigin(), is(REMOTE));
        assertThat(request.getRemote(), is("127.0.0.1"));
        assertThat(request.getMethod(), is("GET"));
        assertThat(request.getRequestUri(), is("http://localhost/"));
        assertThat(request.getScheme(), is("http"));
        assertThat(request.getHost(), is("localhost"));
        assertThat(request.getPort(), is(Optional.of(8080)));
        assertThat(request.getPath(), is("/"));
        assertThat(request.getQuery(), is(emptyString()));
        assertThat(request.getProtocolVersion(), is("HTTP/1.1"));
        assertThat(request.getHeaders().values(), is(empty()));
        assertThat(request.getContentType(), is(""));
        assertThat(request.getCharset(), is(UTF_8));
        assertThat(request.getBody(), is("".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is(emptyString()));
    }

}
