package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.logbook.MockHeaders.of;
import static org.zalando.logbook.Origin.REMOTE;

public final class MockRawHttpResponseTest implements MockHttpMessageTester {

    private final MockRawHttpResponse unit = MockRawHttpResponse.create();

    @Test
    void shouldDelegate() throws IOException {
        verifyResponse(unit);
    }

    @Test
    void shouldDelegateWithBody() throws IOException {
        final HttpResponse response = unit.withBody();
        verifyResponse(response);
        assertThat(response.getBody(), is("".getBytes(UTF_8)));
        assertThat(response.getBodyAsString(), is(""));
    }

    @Test
    void shouldUseNonDefaultStatusCode() {
        final MockRawHttpResponse unit = MockRawHttpResponse.create().withStatus(201);

        assertThat(unit.getStatus(), is(201));
    }

    @Test
    void shouldSupportWith() {
        assertWith(unit, MockRawHttpResponse::withProtocolVersion, "HTTP/2", RawHttpResponse::getProtocolVersion);
        assertWith(unit, MockRawHttpResponse::withOrigin, REMOTE, RawHttpResponse::getOrigin);
        assertWith(unit, MockRawHttpResponse::withStatus, 404, RawHttpResponse::getStatus);
        assertWith(unit, MockRawHttpResponse::withHeaders, of("Accept", "text/plain"), RawHttpResponse::getHeaders);
        assertWith(unit, MockRawHttpResponse::withContentType, "text/xml", RawHttpResponse::getContentType);
        assertWith(unit, MockRawHttpResponse::withCharset, ISO_8859_1, RawHttpResponse::getCharset);
        assertWith(unit, MockRawHttpResponse::withBodyAsString, "Hello",
                throwingFunction(MockRawHttpResponse::getBodyAsString));
    }

}
