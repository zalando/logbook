package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMessage;
import static io.netty.handler.codec.http.HttpUtil.getContentLength;

final class Offering implements State {

    @Override
    public State without() {
        return new Unbuffered();
    }

    @Override
    public State buffer(final HttpMessage message, final ByteBuf content) {
        if (content.equals(Unpooled.EMPTY_BUFFER)) {
            // saves us from allocating an unnecessary buffer
            return this;
        }

        final int contentLength = getContentLength(message, 2048);
        return new Buffering(contentLength).buffer(message, content);
    }

}
