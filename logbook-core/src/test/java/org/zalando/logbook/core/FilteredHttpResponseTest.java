package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.MockHttpResponse;
import org.zalando.logbook.api.HttpHeaders;
import org.zalando.logbook.api.HttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

final class FilteredHttpResponseTest {

    private final HttpResponse unit = new FilteredHttpResponse(MockHttpResponse.create()
            .withHeaders(HttpHeaders.empty()
                    .update("Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671")
                    .update("Accept", "text/plain"))
            .withBodyAsString("My secret is s3cr3t"),
            HeaderFilters.authorization(),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    void shouldFilterAuthorizationHeader() {
        assertThat(unit.getHeaders())
                .hasEntrySatisfying("Authorization", values -> assertThat(values).contains("XXX"));
    }

    @Test
    void shouldFilterAuthorizationHeaderWithBody() throws IOException {
        assertThat(unit.withBody().getHeaders())
                .hasEntrySatisfying("Authorization", values -> assertThat(values).contains("XXX"));
    }

    @Test
    void shouldFilterAuthorizationHeaderWithoutBody() {
        assertThat(unit.withoutBody().getHeaders())
                .hasEntrySatisfying("Authorization", values -> assertThat(values).contains("XXX"));
    }

    @Test
    void shouldNotFilterAcceptHeader() {
        assertThat(unit.getHeaders())
                .hasEntrySatisfying("Accept", values -> assertThat(values).contains("text/plain"));
    }

    @Test
    void shouldFilterBody() throws IOException {
        assertThat(unit.getBodyAsString()).isEqualTo("My secret is f4k3");
    }

    @Test
    void shouldFilterBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset())).isEqualTo("My secret is f4k3");
    }

}
