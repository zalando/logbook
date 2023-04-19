package org.zalando.logbook.test;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpRequest;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.logbook.HttpHeaders.of;
import static org.zalando.logbook.Origin.LOCAL;

final class MockHttpRequestTest implements MockHttpMessageTester {

    private final MockHttpRequest unit = MockHttpRequest.create();

    @Test
    void shouldUseDefaults() throws IOException {
        verifyRequest(unit);

        assertThat(unit.getBody()).isEqualTo("".getBytes(UTF_8));
        assertThat(unit.getBodyAsString()).isEmpty();
    }

    @Test
    void shouldSupportWith() {
        assertWith(unit, MockHttpRequest::withProtocolVersion, "HTTP/2", HttpRequest::getProtocolVersion);
        assertWith(unit, MockHttpRequest::withOrigin, LOCAL, HttpRequest::getOrigin);
        assertWith(unit, MockHttpRequest::withRemote, "192.168.0.1", HttpRequest::getRemote);
        assertWith(unit, MockHttpRequest::withMethod, "POST", HttpRequest::getMethod);
        assertWith(unit, MockHttpRequest::withScheme, "https", HttpRequest::getScheme);
        assertWith(unit, MockHttpRequest::withHost, "example.org", HttpRequest::getHost);
        assertWith(unit, MockHttpRequest::withPort, Optional.of(443), HttpRequest::getPort);
        assertWith(unit, MockHttpRequest::withPath, "/index.html", HttpRequest::getPath);
        assertWith(unit, MockHttpRequest::withQuery, "?", HttpRequest::getQuery);
        assertWith(unit, MockHttpRequest::withHeaders, of("Accept", "text/plain"), HttpRequest::getHeaders);
        assertWith(unit, MockHttpRequest::withContentType, "text/xml", HttpRequest::getContentType);
        assertWith(unit, MockHttpRequest::withCharset, ISO_8859_1, HttpRequest::getCharset);
        assertWith(unit, MockHttpRequest::withBodyAsString, "Hello", throwingFunction(HttpRequest::getBodyAsString));
    }

    @Test
    void shouldSupportWithBody() throws IOException {
        assertThat(unit.withBodyAsString("Hello").withBody().getBodyAsString()).isEqualTo("Hello");
    }

    @Test
    void shouldSupportWithoutBody() {
        unit.withoutBody();

        assertThat(unit.getBodyAsString()).isEmpty();
    }

}
