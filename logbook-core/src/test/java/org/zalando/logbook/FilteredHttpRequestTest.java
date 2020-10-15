package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

final class FilteredHttpRequestTest {

    private final HttpRequest unit = new FilteredHttpRequest(MockHttpRequest.create()
            .withQuery("password=1234&limit=1")
            .withHeaders(HttpHeaders.empty()
                    .update("Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671")
                    .update("Accept", "text/plain"))
            .withBodyAsString("My secret is s3cr3t")
            .withPath("/endpoint/secret/action"),
            QueryFilters.replaceQuery("password", "unknown"),
            PathFilters.replace("/endpoint/{secrets}/action", "XXX"),
            HeaderFilters.authorization(),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    void shouldNotFailOnInvalidUri() {
        final FilteredHttpRequest invalidRequest = new FilteredHttpRequest(
                MockHttpRequest.create()
                        .withPath("/login")
                        .withQuery("file=.|.%2F.|.%2Fetc%2Fpasswd"),
                QueryFilters.replaceQuery("file", "unknown"),
                PathFilter.none(),
                HeaderFilter.none(),
                BodyFilter.none());

        assertThat(invalidRequest.getRequestUri()).endsWith("/login?file=unknown");
        assertThat(invalidRequest.getPath()).isEqualTo("/login");
        assertThat(invalidRequest.getQuery()).isEqualTo("file=unknown");
    }

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
    void shouldNotFilterEmptyQueryString() {
        final FilteredHttpRequest request = new FilteredHttpRequest(MockHttpRequest.create(),
                $ -> "*",
                PathFilter.none(),
                HeaderFilter.none(),
                BodyFilter.none());

        assertThat(request.getRequestUri()).isEqualTo("http://localhost/");
        assertThat(request.getQuery()).isEmpty();
    }

    @Test
    void shouldFilterPasswordParameter() {
        assertThat(unit.getRequestUri()).isEqualTo("http://localhost/endpoint/XXX/action?password=unknown&limit=1");
        assertThat(unit.getQuery()).isEqualTo("password=unknown&limit=1");
    }

    @Test
    void shouldFilterBody() throws IOException {
        assertThat(unit.getBodyAsString()).isEqualTo("My secret is f4k3");
    }

    @Test
    void shouldFilterBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset())).isEqualTo("My secret is f4k3");
    }

    @Test
    void shouldFilterPath() {
        assertThat(unit.getPath()).isEqualTo("/endpoint/XXX/action");
    }

}
