package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class ByteStreamsTest {

    @Test
    void shouldCollectStreamToByteArray() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)));

        assertThat(new String(bytes, UTF_8), is("Hello World!"));
    }

    @Test
    void shouldCopyStreams() throws IOException {
        final ByteArrayOutputStream to = new ByteArrayOutputStream();
        ByteStreams.copy(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), to);

        assertThat(new String(to.toByteArray(), UTF_8), is("Hello World!"));
    }

}
