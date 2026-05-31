package org.zalando.logbook.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.Origin.REMOTE;

class ResponseUnitTest {

    @Test
    void shouldPreserveSyntheticHttp2HeadersOnHttp11Channel() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpResp.headers().add("content-type", "text/plain");

        Response response = new Response(context(channel()), REMOTE, httpResp);
        HttpHeaders headers = response.getHeaders();

        assertThat(headers.keySet())
                .contains(
                        HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.PATH.text().toString(),
                        "content-type");
    }

    @Test
    void shouldStripSyntheticHttp2HeadersFromHttp2StreamChannel() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpResp.headers().add("content-type", "text/plain");

        Response response = new Response(context(http2Channel()), REMOTE, httpResp);
        HttpHeaders headers = response.getHeaders();

        assertThat(headers.keySet())
                .doesNotContain(
                        HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.PATH.text().toString())
                .contains("content-type");
    }

    @Test
    void shouldNotStripNonSyntheticHeaders() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        httpResp.headers().add("content-type", "application/json");
        httpResp.headers().add("x-custom", "value");

        Response response = new Response(context(channel()), REMOTE, httpResp);

        assertThat(response.getHeaders().keySet()).contains("content-type", "x-custom");
    }

    @Test
    void shouldReturnHttp2ProtocolVersionOnHttp2StreamChannel() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        Response response = new Response(context(http2Channel()), REMOTE, httpResp);

        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/2.0");
    }

    @Test
    void shouldReturnHttp11ProtocolVersionOnNonHttp2Channel() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        Response response = new Response(context(channel()), REMOTE, httpResp);

        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void shouldReturnCorrectStatusCode() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);

        Response response = new Response(context(channel()), REMOTE, httpResp);

        assertThat(response.getStatus()).isEqualTo(404);
    }

    private static ChannelHandlerContext context(final EmbeddedChannel channel) {
        final String name = "probe";
        channel.pipeline().addLast(name, new ChannelInboundHandlerAdapter());
        return channel.pipeline().context(name);
    }

    private static EmbeddedChannel channel() {
        return new EmbeddedChannel();
    }

    private static EmbeddedChannel http2Channel() {
        return new TestHttp2StreamChannel();
    }

    private static final class TestHttp2StreamChannel extends EmbeddedChannel implements Http2StreamChannel {
        @Override
        public Http2FrameStream stream() {
            return null;
        }
    }
}
