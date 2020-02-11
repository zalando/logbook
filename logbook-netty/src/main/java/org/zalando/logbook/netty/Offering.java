package org.zalando.logbook.netty;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;

import static io.netty.handler.codec.http.HttpUtil.getContentLength;
import static io.netty.handler.codec.http.LastHttpContent.EMPTY_LAST_CONTENT;

final class Offering implements State {

    @Override
    public State without() {
        return new Unbuffered();
    }

    @Override
    public State buffer(final HttpMessage message, final HttpContent content) {
        if (content.equals(EMPTY_LAST_CONTENT)) {
            // saves us from allocating an unnecessary buffer
            return this;
        }

        final int contentLength = getContentLength(message, 2048);
        return new Buffering(contentLength).buffer(message, content);
    }

}
