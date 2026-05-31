package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.HttpConversionUtil;

final class SyntheticHttp2Headers {

    private SyntheticHttp2Headers() {
    }

    static HttpHeaders stripIfHttp2Stream(final Channel channel, final HttpHeaders headers) {
        if (channel instanceof Http2StreamChannel) {
            headers.remove(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
            headers.remove(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text());
            headers.remove(HttpConversionUtil.ExtensionHeaderNames.PATH.text());
        }
        return headers;
    }
}
