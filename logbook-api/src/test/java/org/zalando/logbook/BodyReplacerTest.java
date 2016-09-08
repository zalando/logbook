package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.BodyReplacer.compound;
import static org.zalando.logbook.BodyReplacer.replaceBody;

public final class BodyReplacerTest {

    @Test
    public void shouldReplaceWith() {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("image/png");

        final String body = unit.replace(message);

        assertThat(body, is("<content>"));
    }

    @Test
    public void shouldNotReplaceWith() throws IOException {
        final BodyReplacer<HttpMessage> unit = replaceBody(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");
        when(message.getBodyAsString()).thenReturn("Hello, world!");

        final String body = unit.replace(message);

        assertThat(body, is(nullValue()));
    }

    @Test
    public void shouldStopOnFirstReplacerThatReplaced() throws IOException {
        final BodyReplacer<HttpMessage> unit = compound(
                replaceBody(m -> m.getContentType().startsWith("text/"), "<text>"),
                replaceBody(m -> m.getContentType().endsWith("plain"), "<plain-text>"));
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");

        final String body = unit.replace(message);

        assertThat(body, is("<text>"));
    }

}