package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class RequestFiltersTest {

    @Test
    void shouldReplaceImageBodyByDefault() throws IOException {
        final RequestFilter filter = RequestFilters.defaultValue();

        final HttpRequest request = filter.filter(MockHttpRequest.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        request.withBody();

        assertThat(request.getContentType(), is("image/png"));
        assertThat(request.getContentType(), is("image/png"));
        assertThat(request.getBody(), is("<binary>".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is("<binary>"));
    }

    @Test
    void shouldNotReplaceTextByDefault() throws IOException {
        final RequestFilter filter = RequestFilters.defaultValue();

        final HttpRequest request = filter.filter(MockHttpRequest.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        request.withBody();

        assertThat(request.getBody(), is("Hello".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is("Hello"));
    }

}
