package org.zalando.logbook.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.net.SocketAddress;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.logbook.Origin.LOCAL;
import static org.zalando.logbook.Origin.REMOTE;

class RequestUnitTest {

    @Test
    void shouldBeDefaultRequest() {
        HttpRequest req = request("/test?a=b", headers(CONTENT_TYPE.toString(), "text/plain"));
        EmbeddedChannel channel = channel(null, LocalAddress.ANY, sslHandler());
        ChannelHandlerContext context = context(channel);

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

        Request localRequest = new Request(context, LOCAL, request("/test?a=b", headers(HOST.toString(), "localhost")));
        assertThat(localRequest.getHost()).isEqualTo("localhost");
    }

    @Test
    void shouldHandleUriWithoutQuery() {
        Request remoteRequest = new Request(
                context(channel(null, LocalAddress.ANY, sslHandler())),
                REMOTE,
                request("/test", headers(CONTENT_TYPE.toString(), "text/plain")));

        assertThat(remoteRequest.getRequestUri()).isEqualTo("https://unknown/test");
        assertThat(remoteRequest.getPath()).isEqualTo("/test");
        assertThat(remoteRequest.getQuery()).isEqualTo("");
    }

    @Test
    void shouldHandleMaliciousRequests() {
        Request remoteRequest = new Request(
                context(channel(null, LocalAddress.ANY, sslHandler())),
                REMOTE,
                request("/libs/dam/merge/metadata.json;%0A.json?path=<h1>Rhack&;%0A.inc.js",
                        headers(CONTENT_TYPE.toString(), "text/plain")));

        assertThat(remoteRequest.getRequestUri()).isEqualTo("https://unknown/libs/dam/merge/metadata.json;\n.json?path=<h1>Rhack&;%0A.inc.js");
        assertThat(remoteRequest.getPath()).isEqualTo("/libs/dam/merge/metadata.json;\n.json");
        assertThat(remoteRequest.getQuery()).isEqualTo("path=<h1>Rhack&;%0A.inc.js");
    }

    @Test
    void shouldHandleNullRemoteAddress() {
        Request remoteRequest = new Request(
                context(channel(null, null)),
                REMOTE,
                request("/test?a=b", new DefaultHttpHeaders()));

        assertThat(remoteRequest.getRemote()).isNull();
    }

    @Test
    void shouldReturnHttp2ProtocolVersionOnHttp2StreamChannel() {
        Request request = new Request(
                context(http2Channel(null, LocalAddress.ANY)),
                REMOTE,
                request("/", new DefaultHttpHeaders()));

        assertThat(request.getProtocolVersion()).isEqualTo("HTTP/2.0");
    }

    @Test
    void shouldReturnHttpsSchemeWhenParentChannelHasSslHandler() {
        EmbeddedChannel parent = channel(null, null, sslHandler());
        EmbeddedChannel child = channel(parent, LocalAddress.ANY);
        Request request = new Request(context(child), REMOTE, request("/", new DefaultHttpHeaders()));

        assertThat(request.getScheme()).isEqualTo("https");
    }

    @Test
    void shouldReturnHttpSchemeWhenParentChannelHasNoSslHandler() {
        EmbeddedChannel parent = channel(null, null);
        EmbeddedChannel child = channel(parent, LocalAddress.ANY);
        Request request = new Request(context(child), REMOTE, request("/", new DefaultHttpHeaders()));

        assertThat(request.getScheme()).isEqualTo("http");
    }

    @Test
    void shouldPreserveSyntheticHttp2HeadersOnHttp11Channel() {
        DefaultHttpRequest httpReq = new DefaultHttpRequest(HTTP_1_1, GET, "/");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpReq.headers().add("content-type", "text/plain");

        Request request = new Request(context(channel(null, LocalAddress.ANY)), REMOTE, httpReq);

        assertThat(request.getHeaders().keySet())
                .contains(
                        HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(),
                        HttpConversionUtil.ExtensionHeaderNames.PATH.text().toString(),
                        "content-type");
    }

    @Test
    void shouldStripSyntheticHttp2HeadersFromHttp2StreamChannel() {
        DefaultHttpRequest httpReq = new DefaultHttpRequest(HTTP_1_1, GET, "/");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        httpReq.headers().add(HttpConversionUtil.ExtensionHeaderNames.PATH.text(), "/real");
        httpReq.headers().add("content-type", "text/plain");

        Request request = new Request(context(http2Channel(null, LocalAddress.ANY)), REMOTE, httpReq);

        assertThat(request.getHeaders().keySet())
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

        Request request = new Request(context(channel(null, LocalAddress.ANY)), REMOTE, httpReq);

        assertThat(request.getHeaders().keySet()).contains("content-type", "x-custom");
    }

    private static HttpRequest request(final String uri, final HttpHeaders headers) {
        DefaultHttpRequest request = new DefaultHttpRequest(HTTP_1_1, HttpMethod.GET, uri);
        request.headers().set(headers);
        return request;
    }

    private static HttpHeaders headers(final String name, final String value) {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(name, value);
        return headers;
    }

    private static ChannelHandlerContext context(final Channel channel) {
        EmbeddedChannel embeddedChannel = (EmbeddedChannel) channel;
        final String name = "probe";
        embeddedChannel.pipeline().addLast(name, new ChannelInboundHandlerAdapter());
        return embeddedChannel.pipeline().context(name);
    }

    private static EmbeddedChannel channel(final Channel parent, final SocketAddress remoteAddress,
            final ChannelHandler... handlers) {
        return new TestChannel(parent, remoteAddress, handlers);
    }

    private static EmbeddedChannel http2Channel(final Channel parent, final SocketAddress remoteAddress,
            final ChannelHandler... handlers) {
        return new TestHttp2StreamChannel(parent, remoteAddress, handlers);
    }

    private static SslHandler sslHandler() {
        try {
            return new SslHandler(SSLContext.getDefault().createSSLEngine());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class TestChannel extends EmbeddedChannel {
        private final Channel parent;
        private final SocketAddress remoteAddress;

        private TestChannel(final Channel parent, final SocketAddress remoteAddress, final ChannelHandler... handlers) {
            super(handlers);
            this.parent = parent;
            this.remoteAddress = remoteAddress;
        }

        @Override
        public Channel parent() {
            return parent;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return remoteAddress;
        }
    }

    private static final class TestHttp2StreamChannel extends TestChannel implements Http2StreamChannel {
        private TestHttp2StreamChannel(final Channel parent, final SocketAddress remoteAddress,
                final ChannelHandler... handlers) {
            super(parent, remoteAddress, handlers);
        }

        @Override
        public Http2FrameStream stream() {
            return null;
        }
    }
}
