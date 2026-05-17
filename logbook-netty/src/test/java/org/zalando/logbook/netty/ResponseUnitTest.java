package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.junit.jupiter.api.Test;
import org.zalando.logbook.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.REMOTE;

class ResponseUnitTest {

    @Test
    void shouldStripSyntheticHttp2HeadersFromGetHeaders() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpResp.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpResp.headers().add("content-type", "text/plain");
        Response response = new Response(mock(ChannelHandlerContext.class), REMOTE, httpResp);
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
        Response response = new Response(mock(ChannelHandlerContext.class), REMOTE, httpResp);
        HttpHeaders headers = response.getHeaders();
        assertThat(headers.keySet()).contains("content-type", "x-custom");
    }

    @Test
    void shouldReturnHttp2ProtocolVersionOnHttp2StreamChannel() {
        Http2StreamChannel streamChannel = mock(Http2StreamChannel.class);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(streamChannel);

        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        Response response = new Response(ctx, REMOTE, httpResp);
        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/2.0");
    }

    @Test
    void shouldReturnHttp11ProtocolVersionOnNonHttp2Channel() {
        Channel channel = mock(Channel.class);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        Response response = new Response(ctx, REMOTE, httpResp);
        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void shouldReturnProtocolVersionWhenContextIsNull() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // Uses the two-arg constructor (context = null)
        Response response = new Response(REMOTE, httpResp);
        assertThat(response.getProtocolVersion()).isEqualTo("HTTP/1.1");
    }

    @Test
    void shouldReturnCorrectStatusCode() {
        DefaultHttpResponse httpResp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        Response response = new Response(REMOTE, httpResp);
        assertThat(response.getStatus()).isEqualTo(404);
    }
}
