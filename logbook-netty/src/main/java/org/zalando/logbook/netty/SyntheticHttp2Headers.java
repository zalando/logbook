package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;

final class SyntheticHttp2Headers {

    private SyntheticHttp2Headers() {
    }

    static HttpHeaders stripIfHttp2Stream(final Channel channel, final HttpHeaders headers) {
        if (isHttp2Stream(channel)) {
            Http2.strip(headers);
        }
        return headers;
    }

    static boolean isHttp2Stream(final Channel channel) {
        for (Class<?> type = channel.getClass(); type != null; type = type.getSuperclass()) {
            for (final Class<?> implemented : type.getInterfaces()) {
                if (implemented.getName().equals("io.netty.handler.codec.http2.Http2StreamChannel")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class Http2 {

        private Http2() {
        }

        private static void strip(final HttpHeaders headers) {
            for (final io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames name
                    : io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames.values()) {
                headers.remove(name.text());
            }
        }
    }
}
