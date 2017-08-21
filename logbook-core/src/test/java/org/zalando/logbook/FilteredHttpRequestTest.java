package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public final class FilteredHttpRequestTest {

    private final HttpRequest unit = new FilteredHttpRequest(MockHttpRequest.create()
            .withQuery("password=1234&limit=1")
            .withHeaders(MockHeaders.of(
                    "Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671",
                    "Accept", "text/plain"))
            .withBodyAsString("My secret is s3cr3t"),
            QueryFilters.replaceQuery("password", "unknown"),
            HeaderFilters.authorization(),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    void shouldNotFailOnInvalidUri() {
        final FilteredHttpRequest invalidRequest = new FilteredHttpRequest(
                MockHttpRequest.create()
                        .withPath("/login")
                        .withQuery("file=.|.%2F.|.%2Fetc%2Fpasswd"),
                QueryFilters.replaceQuery("file", "unknown"),
                HeaderFilter.none(),
                BodyFilter.none());

        assertThat(invalidRequest.getRequestUri(), endsWith("/login?file=unknown"));
        assertThat(invalidRequest.getPath(), is("/login"));
        assertThat(invalidRequest.getQuery(), is("file=unknown"));
    }

    @Test
    void shouldFilterAuthorizationHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    void shouldNotFilterAcceptHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Accept"), contains("text/plain")));
    }

    @Test
    void shouldNotFilterEmptyQueryString() {
        final FilteredHttpRequest request = new FilteredHttpRequest(MockHttpRequest.create(),
                $ -> "*",
                HeaderFilter.none(),
                BodyFilter.none());

        assertThat(request.getRequestUri(), is("http://localhost/"));
        assertThat(request.getQuery(), is(emptyString()));
    }

    @Test
    void shouldFilterPasswordParameter() {
        assertThat(unit.getRequestUri(), is("http://localhost/?password=unknown&limit=1"));
        assertThat(unit.getQuery(), is("password=unknown&limit=1"));
    }

    @Test
    void shouldFilterBody() throws IOException {
        assertThat(unit.getBodyAsString(), is("My secret is f4k3"));
    }

    @Test
    void shouldFilterBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset()), is("My secret is f4k3"));
    }

}
