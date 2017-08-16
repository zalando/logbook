package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ByteStreamsTest {

    @Test
    public void shouldCollectStreamToByteArray() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)));

        assertThat(new String(bytes, UTF_8), is("Hello World!"));
    }

    @Test
    public void shouldCopyStreams() throws IOException {
        final ByteArrayOutputStream to = new ByteArrayOutputStream();
        ByteStreams.copy(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), to);

        assertThat(new String(to.toByteArray(), UTF_8), is("Hello World!"));
    }

}
