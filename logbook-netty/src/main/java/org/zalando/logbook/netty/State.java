package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMessage;

interface State {

    default State with() {
        return this;
    }

    default State without() {
        return this;
    }

    default State buffer(
            final HttpMessage message, final ByteBuf content) {
        return this;
    }

    default byte[] getBody() {
        return new byte[0];
    }

}
