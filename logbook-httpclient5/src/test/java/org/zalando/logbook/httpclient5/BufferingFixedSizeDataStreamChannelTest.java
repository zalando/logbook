package org.zalando.logbook.httpclient5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

final class BufferingFixedSizeDataStreamChannelTest {

    private ByteBuffer buffer;
    private BufferingFixedSizeDataStreamChannel channel;
    private final byte[] result = "b".getBytes(UTF_8);
    private final byte[] data = "a".getBytes(UTF_8);

    @BeforeEach
    void setUp() {
        channel = new BufferingFixedSizeDataStreamChannel(result);
    }

    @AfterEach
    void tearDown() {
        if (buffer != null) buffer.clear();
    }

    @Test
    void testHeapByteBuffer() {
        buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        assertThat(result).isEqualTo(data);
    }

    @Test
    void testOffHeapByteBuffer() {
        buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        buffer.flip();
        channel.write(buffer);
        assertThat(result).isEqualTo(data);
    }

    @Test
    void dummyUnitTest() {
        channel.requestOutput();
        channel.endStream();
        channel.endStream(emptyList());
    }
}