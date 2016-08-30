package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.logbook.MockHttpResponse.response;

public final class ObfuscatedHttpResponseTest {

    private final HttpResponse unit = new ObfuscatedHttpResponse(response()
            .headers(MockHeaders.of(
                    "Authorization", "Bearer 9b7606a6-6838-11e5-8ed4-10ddb1ee7671",
                    "Accept", "text/plain"))
            .body("My secret is s3cr3t")
            .build(),
            Obfuscators.authorization(),
            (contentType, body) -> body.replace("s3cr3t", "f4k3"));

    @Test
    public void shouldObfuscateAuthorizationHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Authorization"), contains("XXX")));
    }

    @Test
    public void shouldNotObfuscateAcceptHeader() {
        assertThat(unit.getHeaders(), hasEntry(equalTo("Accept"), contains("text/plain")));
    }

    @Test
    public void shouldObfuscateBody() throws IOException {
        assertThat(unit.getBodyAsString(), is("My secret is f4k3"));
    }

    @Test
    public void shouldObfuscateBodyContent() throws IOException {
        assertThat(new String(unit.getBody(), unit.getCharset()), is("My secret is f4k3"));
    }

}
