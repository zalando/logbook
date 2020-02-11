package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;

final class Buffering implements State {

    private final ByteBuf buffer;

    Buffering(final int initialCapacity) {
        this.buffer = Unpooled.buffer(initialCapacity);
    }

    @Override
    public State without() {
        return new Ignoring(this);
    }

    @Override
    public State buffer(final HttpMessage message, final HttpContent content) {
        final ByteBuf source = content.content();
        final int index = source.readerIndex();
        source.readBytes(buffer, source.readableBytes());
        source.readerIndex(index);
        return this;
    }

    @Override
    public byte[] getBody() {
        final int length = buffer.readableBytes();

        if (length == buffer.capacity()) {
            return buffer.array();
        }

        final byte[] target = new byte[length];
        System.arraycopy(buffer.array(), 0, target, 0, length);
        return target;
    }

}
