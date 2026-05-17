package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.Test;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;


/**
 * @author sokomishalov
 */
public class RequestUnitTest {

    @Test
    void shouldBeDefaultRequest() {

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/test?a=b");
        when(req.headers()).thenReturn(new DefaultHttpHeaders().add(CONTENT_TYPE, "text/plain"));
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        ChannelHandlerContext context = mockChannelHandlerContext();

        Request remoteRequest = new Request(context, REMOTE, req);

        assertThat(remoteRequest.getScheme()).isEqualTo("https");
        assertThat(remoteRequest.getHost()).isEqualTo("unknown");
        assertThat(remoteRequest.getPort()).isEmpty();
        assertThat(remoteRequest.getContentType()).isEqualTo("text/plain");
        assertThat(remoteRequest.getCharset()).isEqualTo(UTF_8);
        assertThat(remoteRequest.getRemote()).isEqualTo("local:any");
        assertThat(remoteRequest.getRequestUri()).isEqualTo("https://unknown/test?a=b");
        assertThat(remoteRequest.getOrigin()).isEqualTo(REMOTE);
        assertThat(remoteRequest.getMethod()).isEqualTo("GET");
        assertThat(remoteRequest.getPath()).isEqualTo("/test");
        assertThat(remoteRequest.getQuery()).isEqualTo("a=b");
        assertThat(remoteRequest.getProtocolVersion()).isEqualTo("HTTP/1.1");


        when(req.headers()).thenReturn(new DefaultHttpHeaders().add(HOST, "localhost"));
        Request localRequest = new Request(context, LOCAL, req);

        assertThat(localRequest.getHost()).isEqualTo("localhost");
    }

    @Test
    void shouldHandleUriWithoutQuery() {

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/test");
        when(req.headers()).thenReturn(new DefaultHttpHeaders().add(CONTENT_TYPE, "text/plain"));
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        ChannelHandlerContext context = mockChannelHandlerContext();

        Request remoteRequest = new Request(context, REMOTE, req);

        assertThat(remoteRequest.getRequestUri()).isEqualTo("https://unknown/test");
        assertThat(remoteRequest.getPath()).isEqualTo("/test");
        assertThat(remoteRequest.getQuery()).isEqualTo("");
    }

    @Test
    void shouldHandleMaliciousRequests() {

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/libs/dam/merge/metadata.json;%0A.json?path=<h1>Rhack&;%0A.inc.js");
        when(req.headers()).thenReturn(new DefaultHttpHeaders().add(CONTENT_TYPE, "text/plain"));
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        ChannelHandlerContext context = mockChannelHandlerContext();

        Request remoteRequest = new Request(context, REMOTE, req);

        assertThat(remoteRequest.getRequestUri()).isEqualTo("https://unknown/libs/dam/merge/metadata.json;\n" +
                ".json?path=<h1>Rhack&;%0A.inc.js");
        assertThat(remoteRequest.getPath()).isEqualTo("/libs/dam/merge/metadata.json;\n" +
                ".json");
        assertThat(remoteRequest.getQuery()).isEqualTo("path=<h1>Rhack&;%0A.inc.js");
    }

    @Test
    void shouldHandleNullRemoteAddress() {
        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/test?a=b");

        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(mock(Channel.class));
        when(context.channel().remoteAddress()).thenReturn(null);

        Request remoteRequest = new Request(context, REMOTE, req);
        assertThat(remoteRequest.getRemote()).isEqualTo(null);
    }

    @Test
    void shouldReturnHttp2ProtocolVersionOnHttp2StreamChannel() {
        Http2StreamChannel streamChannel = mock(Http2StreamChannel.class);
        when(streamChannel.parent()).thenReturn(null);
        when(streamChannel.pipeline()).thenReturn(mock(ChannelPipeline.class));
        when(streamChannel.remoteAddress()).thenReturn(LocalAddress.ANY);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(streamChannel);

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/");
        when(req.headers()).thenReturn(new DefaultHttpHeaders());
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Request request = new Request(ctx, REMOTE, req);
        assertThat(request.getProtocolVersion()).isEqualTo("HTTP/2.0");
    }

    @Test
    void shouldReturnHttpsSchemeWhenParentChannelHasSslHandler() {
        Channel parentChannel = mock(Channel.class);
        ChannelPipeline parentPipeline = mock(ChannelPipeline.class);
        when(parentChannel.pipeline()).thenReturn(parentPipeline);
        when(parentPipeline.get(SslHandler.class)).thenReturn(mock(SslHandler.class));

        Channel streamChannel = mock(Channel.class);
        when(streamChannel.parent()).thenReturn(parentChannel);
        when(streamChannel.pipeline()).thenReturn(mock(ChannelPipeline.class));
        when(streamChannel.remoteAddress()).thenReturn(LocalAddress.ANY);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(streamChannel);

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/");
        when(req.headers()).thenReturn(new DefaultHttpHeaders());
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Request request = new Request(ctx, REMOTE, req);
        assertThat(request.getScheme()).isEqualTo("https");
    }

    @Test
    void shouldReturnHttpSchemeWhenParentChannelHasNoSslHandler() {
        Channel parentChannel = mock(Channel.class);
        ChannelPipeline parentPipeline = mock(ChannelPipeline.class);
        when(parentChannel.pipeline()).thenReturn(parentPipeline);
        when(parentPipeline.get(SslHandler.class)).thenReturn(null);

        Channel streamChannel = mock(Channel.class);
        when(streamChannel.parent()).thenReturn(parentChannel);
        when(streamChannel.pipeline()).thenReturn(mock(ChannelPipeline.class));
        when(streamChannel.remoteAddress()).thenReturn(LocalAddress.ANY);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(streamChannel);

        HttpRequest req = mock(HttpRequest.class);
        when(req.uri()).thenReturn("/");
        when(req.headers()).thenReturn(new DefaultHttpHeaders());
        when(req.method()).thenReturn(HttpMethod.GET);
        when(req.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

        Request request = new Request(ctx, REMOTE, req);
        assertThat(request.getScheme()).isEqualTo("http");
    }

    @Test
    void shouldStripSyntheticHttp2HeadersFromGetHeaders() {
        DefaultHttpRequest httpReq = new DefaultHttpRequest(HTTP_1_1, GET, "/");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpReq.headers().add("content-type", "text/plain");
        Request request = new Request(mockChannelHandlerContext(), REMOTE, httpReq);
        org.zalando.logbook.HttpHeaders headers = request.getHeaders();
        assertThat(headers.keySet())
            .doesNotContain(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text().toString(),
                HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(),
                HttpConversionUtil.ExtensionHeaderNames.PATH.text().toString())
            .contains("content-type");
    }

    @Test
    void shouldNotStripNonSyntheticHeadersHttp11() {
        DefaultHttpRequest httpReq = new DefaultHttpRequest(HTTP_1_1, GET, "/");
        httpReq.headers().add("content-type", "application/json");
        httpReq.headers().add("x-custom", "value");
        Request request = new Request(mockChannelHandlerContext(), REMOTE, httpReq);
        org.zalando.logbook.HttpHeaders headers = request.getHeaders();
        assertThat(headers.keySet()).contains("content-type", "x-custom");
    }

    private ChannelHandlerContext mockChannelHandlerContext() {

        ChannelHandlerContext context = mock(ChannelHandlerContext.class);

        when(context.channel()).thenReturn(mock(Channel.class));
        when(context.channel().pipeline()).thenReturn(mock(ChannelPipeline.class));
        when(context.channel().pipeline().get(SslHandler.class)).thenReturn(mock(SslHandler.class));

        when(context.channel().remoteAddress()).thenReturn(LocalAddress.ANY);
        return context;
    }
}
