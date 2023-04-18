package org.zalando.logbook.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class HttpMessageTest {

    @Test
    void shouldDelegateBodyAsStringToBody() throws IOException {
        final HttpMessage message = mock(HttpMessage.class);

        when(message.getCharset()).thenReturn(UTF_8);
        when(message.getBody()).thenReturn("foo".getBytes(UTF_8));
        when(message.getBodyAsString()).thenCallRealMethod();

        assertThat(message.getBodyAsString()).isEqualTo("foo");
    }

    @Test
    void shouldParseDefaultContentType() {
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenCallRealMethod();
        when(message.getCharset()).thenCallRealMethod();

        when(message.getHeaders()).thenReturn(HttpHeaders.of("Content-Type", "application/json; charset=us-ascii"));

        assertThat(message.getContentType()).isEqualTo("application/json");
        assertThat(message.getCharset()).isEqualTo(StandardCharsets.US_ASCII);

        when(message.getHeaders()).thenReturn(HttpHeaders.of("Content-Type", "application/json"));

        assertThat(message.getContentType()).isEqualTo("application/json");
        assertThat(message.getCharset()).isEqualTo(UTF_8);

        when(message.getHeaders()).thenReturn(HttpHeaders.empty());

        assertThat(message.getContentType()).isNull();
        assertThat(message.getCharset()).isEqualTo(UTF_8);
    }

    @Test
    void shouldReturnDefaultProtocolVersion() {
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getProtocolVersion()).thenCallRealMethod();

        assertThat(message.getProtocolVersion()).isEqualTo("HTTP/1.1");
    }

}
