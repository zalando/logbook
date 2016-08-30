package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.LOCAL;

public final class ForwardingHttpResponseTest {

    private final HttpResponse unit = new ForwardingHttpResponse() {
        @Override
        protected HttpResponse delegate() {
            final HttpResponse response = mock(HttpResponse.class);

            when(response.getOrigin()).thenReturn(LOCAL);
            when(response.getStatus()).thenReturn(200);
            when(response.getHeaders()).thenReturn(emptyMap());
            when(response.getContentType()).thenReturn("");
            when(response.getCharset()).thenReturn(UTF_8);

            try {
                when(response.getBody()).thenReturn("".getBytes(UTF_8));
                when(response.getBodyAsString()).thenReturn("");
            } catch (final IOException e) {
                throw new AssertionError(e);
            }

            return response;
        }
    };

    @Test
    public void shouldDelegate() throws IOException {
        assertThat(unit.getOrigin(), is(LOCAL));
        assertThat(unit.getStatus(), is(200));
        assertThat(unit.getHeaders().values(), is(empty()));
        assertThat(unit.getContentType(), is(emptyString()));
        assertThat(unit.getCharset(), is(UTF_8));
        assertThat(unit.getBody(), is("".getBytes(UTF_8)));
        assertThat(unit.getBodyAsString(), is(emptyString()));
        assertNotNull(unit.toString());
    }

}
