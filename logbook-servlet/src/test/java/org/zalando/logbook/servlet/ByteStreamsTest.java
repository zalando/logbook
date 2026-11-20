package org.zalando.logbook.servlet;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class ByteStreamsTest {

    @Test
    void shouldCollectStreamToByteArray() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)));

        assertThat(new String(bytes, UTF_8)).isEqualTo("Hello World!");
    }

    @Test
    void shouldCollectStreamWithLimitUnderMax() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), 50);

        assertThat(new String(bytes, UTF_8)).isEqualTo("Hello World!");
    }

    @Test
    void shouldCollectStreamWithLimitExceeded() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), 5);

        assertThat(bytes).hasSize(6);
        assertThat(new String(bytes, UTF_8)).isEqualTo("Hello ");
    }

    @Test
    void shouldCollectStreamWithNegativeLimit() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), -1);

        assertThat(new String(bytes, UTF_8)).isEqualTo("Hello World!");
    }

    @Test
    void shouldCollectStreamWithMaxLongLimit() throws IOException {
        final byte[] bytes = ByteStreams.toByteArray(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), Long.MAX_VALUE);

        assertThat(new String(bytes, UTF_8)).isEqualTo("Hello World!");
    }

    @Test
    void shouldCopyStreams() throws IOException {
        final ByteArrayOutputStream to = new ByteArrayOutputStream();
        ByteStreams.copy(new ByteArrayInputStream("Hello World!".getBytes(UTF_8)), to);

        assertThat(new String(to.toByteArray(), UTF_8)).isEqualTo("Hello World!");
    }

}
