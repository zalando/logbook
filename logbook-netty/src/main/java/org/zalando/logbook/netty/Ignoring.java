package org.zalando.logbook.netty;

import io.netty.handler.codec.http.HttpContent;
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
    public State buffer(final HttpMessage message, final HttpContent content) {
        buffering.buffer(message, content);
        return this;
    }

}
