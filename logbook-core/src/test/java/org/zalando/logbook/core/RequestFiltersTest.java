package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.HttpRequest;
import org.zalando.logbook.api.RequestFilter;
import org.zalando.logbook.test.MockHttpRequest;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class RequestFiltersTest {

    @Test
    void shouldReplaceImageBodyByDefault() throws IOException {
        final RequestFilter filter = RequestFilters.defaultValue();

        final HttpRequest request = filter.filter(MockHttpRequest.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        request.withBody();

        assertThat(request.getContentType()).isEqualTo("image/png");
        assertThat(request.getBody()).isEqualTo("<binary>".getBytes(UTF_8));
        assertThat(request.getBodyAsString()).isEqualTo("<binary>");
    }

    @Test
    void shouldReplaceImageBodyEvenWithoutBody() throws IOException {
        final RequestFilter filter = RequestFilters.defaultValue();

        final HttpRequest request = filter.filter(MockHttpRequest.create()
                .withContentType("image/png")
                .withBodyAsString("this is an image"));

        request.withoutBody();

        assertThat(request.getContentType()).isEqualTo("image/png");
        assertThat(request.getBody()).isEqualTo("<binary>".getBytes(UTF_8));
        assertThat(request.getBodyAsString()).isEqualTo("<binary>");
    }

    @Test
    void shouldNotReplaceTextByDefault() throws IOException {
        final RequestFilter filter = RequestFilters.defaultValue();

        final HttpRequest request = filter.filter(MockHttpRequest.create()
                .withContentType("text/plain")
                .withBodyAsString("Hello"));

        request.withBody();

        assertThat(request.getBody()).isEqualTo("Hello".getBytes(UTF_8));
        assertThat(request.getBodyAsString()).isEqualTo("Hello");
    }

}
