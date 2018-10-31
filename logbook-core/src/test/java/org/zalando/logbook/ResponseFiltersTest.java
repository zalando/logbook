package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class ResponseFiltersTest {

    @Test
    void shouldReplaceImageBodyByDefault() throws IOException {
        final ResponseFilter filter = ResponseFilters.defaultValue();

        final HttpResponse response = filter.filter(MockHttpResponse.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        response.withBody();

        assertThat(response.getContentType(), is("image/png"));
        assertThat(response.getContentType(), is("image/png"));
        assertThat(response.getBody(), is("<binary>".getBytes(UTF_8)));
        assertThat(response.getBodyAsString(), is("<binary>"));
    }

    @Test
    void shouldNotReplaceTextBodyByDefault() throws IOException {
        final ResponseFilter filter = ResponseFilters.defaultValue();

        final HttpResponse response = filter.filter(MockHttpResponse.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        response.withBody();

        assertThat(response.getBody(), is("Hello".getBytes(UTF_8)));
        assertThat(response.getBodyAsString(), is("Hello"));
    }

}
