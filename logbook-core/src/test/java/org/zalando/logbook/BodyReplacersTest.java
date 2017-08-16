package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.BodyReplacers.replaceBody;

public final class BodyReplacersTest {

    @Test
    void shouldReplaceWith() {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("image/png");

        final String body = unit.replace(message);

        assertThat(body, is("<content>"));
    }

    @Test
    void shouldNotReplaceWith() throws IOException {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");
        when(message.getBodyAsString()).thenReturn("Hello, world!");

        final String body = unit.replace(message);

        assertThat(body, is(nullValue()));
    }

}
