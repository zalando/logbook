package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class RawRequestFiltersTest {

    @Test
    public void shouldReplaceImageBodyByDefault() throws IOException {
        final RawRequestFilter filter = RawRequestFilters.defaultValue();

        final RawHttpRequest request = filter.filter(MockRawHttpRequest.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        assertThat(request.getContentType(), is("image/png"));
        assertThat(request.withBody().getContentType(), is("image/png"));
        assertThat(request.withBody().getBody(), is("<binary>".getBytes(UTF_8)));
        assertThat(request.withBody().getBodyAsString(), is("<binary>"));
    }

    @Test
    public void shouldNotReplaceTextByDefault() throws IOException {
        final RawRequestFilter filter = RawRequestFilters.defaultValue();

        final RawHttpRequest request = filter.filter(MockRawHttpRequest.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        assertThat(request.withBody().getBody(), is("Hello".getBytes(UTF_8)));
        assertThat(request.withBody().getBodyAsString(), is("Hello"));
    }

}