package org.zalando.logbook.httpclient5;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

final class BufferingDynamicSizeDataStreamChannelTest {

    private final BufferingDynamicSizeDataStreamChannel channel = new BufferingDynamicSizeDataStreamChannel();
    private final byte[] data = "hello".getBytes(UTF_8);

    @Test
    void testHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);
        assertThat(channel.getBuffer()).isEqualTo(data);
    }

    @Test
    void testOffHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        buffer.flip();
        channel.write(buffer);
        assertThat(channel.getBuffer()).isEqualTo(data);
    }

    @Test
    void dummyUnitTest() {
        channel.requestOutput();
        channel.endStream();
        channel.endStream(emptyList());
    }

    @Test
    void testHeapByteBufferUsesOnlyReadableRange() {
        byte[] expected = "body".getBytes(UTF_8);
        byte[] backing = "prefixbodytail".getBytes(UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(backing, 6, 4).slice();

        channel.write(buffer);

        assertThat(channel.getBuffer()).isEqualTo(expected);
    }

}