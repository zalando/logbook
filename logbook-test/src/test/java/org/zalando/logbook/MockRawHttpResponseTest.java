package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public final class MockRawHttpResponseTest implements MockHttpMessageTester {

    private final RawHttpResponse unit = MockRawHttpResponse.create();

    @Test
    public void shouldDelegate() throws IOException {
        verifyResponse(unit);
    }

    @Test
    public void shouldDelegateWithBody() throws IOException {
        final HttpResponse response = unit.withBody();
        verifyResponse(response);
        assertThat(response.getBody(), is("".getBytes(UTF_8)));
        assertThat(response.getBodyAsString(), is(""));
    }

    @Test
    public void shouldUseNonDefaultStatusCode() {
        final MockRawHttpResponse unit = MockRawHttpResponse.response().status(201).build();

        assertThat(unit.getStatus(), is(201));
    }

}