package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.zalando.logbook.api.HttpRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LocalRequestTest {

    private byte[] body;

    @BeforeEach
    void setup() throws URISyntaxException {
        body = "test".getBytes();
    }

    @Test
    void shouldResolveLocalhost() {
        final HttpRequest unit = unit(get("http://localhost/"));
        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldIgnoreDefaultHttpPort() {
        final HttpRequest unit = unit(get("http://localhost/"));
        assertThat(unit.getPort()).isEmpty();
    }

    @Test
    void shouldIgnoreDefaultHttpsPort() {
        final HttpRequest unit = unit(get("https://localhost/"));
        assertThat(unit.getPort()).isEmpty();
    }

    @Test
    void canResolvePort() {
        final HttpRequest unit = unit(get("https://localhost:8080/"));
        assertThat(unit.getPort()).hasValue(8080);
    }

    @Test
    void noBody() throws IOException {
        final HttpRequest unit = unit(get("https://localhost:8080/"));
        assertThat(unit.withoutBody().getBody()).asString().isEqualTo("");
    }

    @Test
    void handleDefaultCharset() {
        final HttpRequest unit = unit(get("https://localhost:8080/"));
        assertThat(unit.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void parseCharset() {
        String value = "text/html; charset=utf-16BE";
        MockClientHttpRequest request = new MockClientHttpRequest();
        request.getHeaders().add("Content-Type", value);
        assertThat(unit(request).getCharset()).isEqualTo(StandardCharsets.UTF_16BE);
    }

    private MockClientHttpRequest get(String uri) {
        try {
            return new MockClientHttpRequest(HttpMethod.GET, new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest unit(MockClientHttpRequest request) {
        return new LocalRequest(request, body);
    }
}
