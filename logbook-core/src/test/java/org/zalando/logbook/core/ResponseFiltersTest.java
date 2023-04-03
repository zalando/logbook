package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.api.HttpResponse;
import org.zalando.logbook.api.ResponseFilter;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class ResponseFiltersTest {

    @Test
    void shouldReplaceImageBodyByDefault() throws IOException {
        final ResponseFilter filter = ResponseFilters.defaultValue();

        final HttpResponse response = filter.filter(MockHttpResponse.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        response.withBody();

        assertThat(response.getContentType()).isEqualTo("image/png");
        assertThat(response.getBody()).isEqualTo("<binary>".getBytes(UTF_8));
        assertThat(response.getBodyAsString()).isEqualTo("<binary>");
    }

    @Test
    void shouldReplaceImageBodyEvenWithoutBody() throws IOException {
        final ResponseFilter filter = ResponseFilters.defaultValue();

        final HttpResponse response = filter.filter(MockHttpResponse.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        response.withoutBody();

        assertThat(response.getContentType()).isEqualTo("image/png");
        assertThat(response.getBody()).isEqualTo("<binary>".getBytes(UTF_8));
    }

    @Test
    void shouldNotReplaceTextBodyByDefault() throws IOException {
        final ResponseFilter filter = ResponseFilters.defaultValue();

        final HttpResponse response = filter.filter(MockHttpResponse.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        response.withBody();

        assertThat(response.getBody()).isEqualTo("Hello".getBytes(UTF_8));
        assertThat(response.getBodyAsString()).isEqualTo("Hello");
    }

}
