package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Ignoring implements State {

    private final Buffering buffering;

    @Override
    public State with() {
        return buffering;
    }

    @Override
    public State buffer(final HttpMessage message, final ByteBuf content) {
        buffering.buffer(message, content);
        return this;
    }

}
