package org.zalando.logbook;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.BodyReplacer.compound;

public final class BodyReplacerTest {

    @Test
    public void shouldStopOnFirstReplacerThatReplaced() throws IOException {
        final BodyReplacer<HttpMessage> unit = compound(
                m -> m.getContentType().startsWith("text/") ? "<text>" : null,
                m -> m.getContentType().endsWith("plain") ? "<plain-text>" : null);
        final HttpMessage message = mock(HttpMessage.class);
        when(message.getContentType()).thenReturn("text/plain");

        final String body = unit.replace(message);

        assertThat(body, is("<text>"));
    }

}