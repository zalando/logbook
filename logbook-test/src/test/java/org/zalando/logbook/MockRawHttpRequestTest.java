package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class MockRawHttpRequestTest implements MockHttpMessageTester {

    private final RawHttpRequest unit = MockRawHttpRequest.create();

    @Test
    public void shouldDelegate() throws IOException {
        verifyRequest(unit);
    }

    @Test
    public void shouldDelegateWithBody() throws IOException {
        final HttpRequest request = unit.withBody();
        verifyRequest(request);
        assertThat(request.getBody(), is("".getBytes(UTF_8)));
        assertThat(request.getBodyAsString(), is(""));
    }

    @Test
    public void shouldUseNonDefaultPort() {
        final MockRawHttpRequest unit = MockRawHttpRequest.request().port(8080).build();

        assertThat(unit.getPort(), is(Optional.of(8080)));
    }

}