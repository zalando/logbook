package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class MockHttpRequestTest implements MockHttpMessageTester {

    private final HttpRequest unit = MockHttpRequest.create();

    @Test
    public void shouldUseDefaults() throws IOException {
        verifyRequest(unit);

        assertThat(unit.getBody(), is("".getBytes(UTF_8)));
        assertThat(unit.getBodyAsString(), is(emptyString()));
    }

}