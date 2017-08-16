package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HttpMessageTest {

    @Test
    void shouldDelegateBodyAsStringToBody() throws IOException {
        final HttpMessage message = mock(HttpMessage.class);

        when(message.getCharset()).thenReturn(UTF_8);
        when(message.getBody()).thenReturn("foo".getBytes(UTF_8));
        when(message.getBodyAsString()).thenCallRealMethod();

        assertThat(message.getBodyAsString(), is("foo"));
    }

}
