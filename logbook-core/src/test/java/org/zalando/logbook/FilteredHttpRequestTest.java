package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
    public void shouldNotFailOnInvalidUri() {
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
    public void shouldFilterAuthorizationHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    public void shouldNotFilterAcceptHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Accept"), contains("text/plain")));
    }

    @Test
    public void shouldNotFilterEmptyQueryString() {
        final FilteredHttpRequest request = new FilteredHttpRequest(MockHttpRequest.create(),
                $ -> "*",
                HeaderFilter.none(),
                BodyFilter.none());

        assertThat(request.getRequestUri(), is("http://localhost/"));
        assertThat(request.getQuery(), is(emptyString()));
    }

    @Test
    public void shouldFilterPasswordParameter() {
        assertThat(unit.getRequestUri(), is("http://localhost/?password=unknown&limit=1"));
        assertThat(unit.getQuery(), is("password=unknown&limit=1"));
    }

    @Test
    public void shouldFilterBody() throws IOException {
        assertThat(unit.getBodyAsString(), is("My secret is f4k3"));
    }

    @Test
    public void shouldFilterBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset()), is("My secret is f4k3"));
    }

}
