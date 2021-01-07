package org.zalando.logbook.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalRequestTest {

    private byte[] body;

    @BeforeEach
    void setup() throws URISyntaxException {
        body = "test".getBytes();
    }

    @Test
    void shouldResolveLocalhost() {
        final org.zalando.logbook.HttpRequest unit = unit(get("http://localhost/"));
        assertThat(unit.getRemote()).isEqualTo("localhost");
    }

    @Test
    void shouldIgnoreDefaultHttpPort() {
        final org.zalando.logbook.HttpRequest unit = unit(get("http://localhost/"));
        assertThat(unit.getPort()).isEmpty();
    }

    @Test
    void shouldIgnoreDefaultHttpsPort() {
        final org.zalando.logbook.HttpRequest unit = unit(get("https://localhost/"));
        assertThat(unit.getPort()).isEmpty();
    }

    @Test
    void parseCharset() {
        String value = "text/html; charset=utf-16BE";
        MockClientHttpRequest request = new MockClientHttpRequest();
        request.getHeaders().add("Content-Type", value);
        assertEquals(StandardCharsets.UTF_16BE, unit(request).getCharset());
    }

    private MockClientHttpRequest get(String uri) {
        try {
            return new MockClientHttpRequest(HttpMethod.GET, new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private org.zalando.logbook.HttpRequest unit(MockClientHttpRequest request) {
        return new LocalRequest(request, body);
    }
}
