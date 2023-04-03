package org.zalando.logbook.core;

import org.junit.jupiter.api.Test;
import org.zalando.logbook.api.BodyReplacer;
import org.zalando.logbook.api.HttpMessage;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.core.BodyReplacers.replaceBody;

final class BodyReplacersTest {

    @Test
    void shouldReplaceWith() {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("image/png");

        final String body = unit.replace(message);

        assertThat(body).isEqualTo("<content>");
    }

    @Test
    void shouldNotReplaceWith() throws IOException {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");
        when(message.getBodyAsString()).thenReturn("Hello, world!");

        final String body = unit.replace(message);

        assertThat(body).isNull();
    }

}
