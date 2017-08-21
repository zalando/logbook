package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class RawRequestFiltersTest {

    @Test
    void shouldReplaceImageBodyByDefault() throws IOException {
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
    void shouldNotReplaceTextByDefault() throws IOException {
        final RawRequestFilter filter = RawRequestFilters.defaultValue();

        final RawHttpRequest request = filter.filter(MockRawHttpRequest.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        assertThat(request.withBody().getBody(), is("Hello".getBytes(UTF_8)));
        assertThat(request.withBody().getBodyAsString(), is("Hello"));
    }

}
