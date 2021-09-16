package org.zalando.logbook.openfeign;

import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LocalRequestTest {
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
        Request request = Request.create(
                Request.HttpMethod.GET,
                "https://localhost:8080",
                Collections.emptyMap(),
                Request.Body.create("test", StandardCharsets.UTF_16BE),
                new RequestTemplate()
        );
        assertThat(unit(request).getCharset()).isEqualTo(StandardCharsets.UTF_16BE);
    }

    private Request get(String uri) {
        return Request.create(
                Request.HttpMethod.GET,
                uri,
                Collections.emptyMap(),
                Request.Body.create(new byte[0]),
                new RequestTemplate()
        );
    }

    private HttpRequest unit(Request request) {
        return LocalRequest.create(request);
    }
}
