package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.logbook.MockHeaders.of;
import static org.zalando.logbook.Origin.LOCAL;

public final class MockRawHttpRequestTest implements MockHttpMessageTester {

    private final MockRawHttpRequest unit = MockRawHttpRequest.create();

    @Test
    void shouldDelegate() throws IOException {
        verifyRequest(unit);
    }

    @Test
    void shouldDelegateWithBody() throws IOException {
        final HttpRequest request = unit.withBody();
        verifyRequest(request);
        assertThat(request.getBody(), is("".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is(""));
    }

    @Test
    void shouldUseNonDefaultPort() {
        final MockRawHttpRequest unit = MockRawHttpRequest.create().withPort(Optional.of(8080));

        assertThat(unit.getPort(), is(Optional.of(8080)));
    }

    @Test
    void shouldOptimizeWith() {
        assertWith(unit, MockRawHttpRequest::withProtocolVersion, "HTTP/2", RawHttpRequest::getProtocolVersion);
        assertWith(unit, MockRawHttpRequest::withOrigin, LOCAL, RawHttpRequest::getOrigin);
        assertWith(unit, MockRawHttpRequest::withRemote, "192.168.0.1", RawHttpRequest::getRemote);
        assertWith(unit, MockRawHttpRequest::withMethod, "POST", RawHttpRequest::getMethod);
        assertWith(unit, MockRawHttpRequest::withScheme, "https", RawHttpRequest::getScheme);
        assertWith(unit, MockRawHttpRequest::withHost, "example.org", RawHttpRequest::getHost);
        assertWith(unit, MockRawHttpRequest::withPort, Optional.of(443), RawHttpRequest::getPort);
        assertWith(unit, MockRawHttpRequest::withPath, "/index.html", RawHttpRequest::getPath);
        assertWith(unit, MockRawHttpRequest::withQuery, "?", RawHttpRequest::getQuery);
        assertWith(unit, MockRawHttpRequest::withHeaders, of("Accept", "text/plain"), RawHttpRequest::getHeaders);
        assertWith(unit, MockRawHttpRequest::withContentType, "text/xml", RawHttpRequest::getContentType);
        assertWith(unit, MockRawHttpRequest::withCharset, ISO_8859_1, RawHttpRequest::getCharset);
        assertWith(unit, MockRawHttpRequest::withBodyAsString, "Hello",
                throwingFunction(MockRawHttpRequest::getBodyAsString));
    }

}
