package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

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
        assertThat(unit.getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    void shouldFilterAuthorizationHeaderWithBody() throws IOException {
        assertThat(unit.withBody().getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    void shouldFilterAuthorizationHeaderWithoutBody() {
        assertThat(unit.withoutBody().getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    void shouldNotFilterAcceptHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Accept"), contains("text/plain")));
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
