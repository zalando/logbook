package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Replacer.compound;
import static org.zalando.logbook.Replacer.replaceWith;

public final class ReplacerTest {

    @Test
    public void shouldReplaceWith() {
        final Replacer<HttpMessage> unit = replaceWith(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("image/png");

        final String body = unit.replace(message);

        assertThat(body, is("<content>"));
    }

    @Test
    public void shouldNotReplaceWith() throws IOException {
        final Replacer<HttpMessage> unit = replaceWith(m -> m.getContentType().startsWith("image/"), "<content>");
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");
        when(message.getBodyAsString()).thenReturn("Hello, world!");

        final String body = unit.replace(message);

        assertThat(body, is(nullValue()));
    }

    @Test
    public void shouldStopOnFirstReplacerThatReplaced() throws IOException {
        final Replacer<HttpMessage> unit = compound(
                replaceWith(m -> m.getContentType().startsWith("text/"), "<text>"),
                replaceWith(m -> m.getContentType().endsWith("plain"), "<plain-text>"));
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");

        final String body = unit.replace(message);

        assertThat(body, is("<text>"));
    }

}