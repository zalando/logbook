package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;
import static org.zalando.logbook.Origin.LOCAL;

@AllArgsConstructor(access = PRIVATE)
final class Request implements org.zalando.logbook.HttpRequest, HeaderSupport {

    private final AtomicReference<State> state =
            new AtomicReference<>(new Unbuffered());

    private final ChannelHandlerContext context;
    private final Origin origin;
    private final HttpRequest request;
    private final QueryStringDecoder uriDecoder;

    public Request(
        final ChannelHandlerContext context,
        final Origin origin,
        final HttpRequest request) {
        this(context, origin, request, new QueryStringDecoder(request.uri()));
    }

    @Override
    public String getProtocolVersion() {
        return request.protocolVersion().text();
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public String getRemote() {
        return context.channel().remoteAddress().toString();
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getScheme() {
        final SslHandler handler = context.channel().pipeline().get(SslHandler.class);
        return handler == null ? "http" : "https";
    }

    @Override
    public String getHost() {
        final String host = request.headers().get(HOST);
        if (host == null) {
            return extractAddress().map(InetSocketAddress::getHostString).orElse("unknown");
        } else {
            return stripPortIfNecessary(host);
        }
    }

    @Override
    public Optional<Integer> getPort() {
        return extractAddress().map(InetSocketAddress::getPort);
    }

    private String stripPortIfNecessary(String host) {
        final int separator = host.indexOf(":");
        return separator == -1 ? host : host.substring(0, separator);
    }

    private Optional<InetSocketAddress> extractAddress() {
        final Channel channel = context.channel();
        final SocketAddress address = origin == LOCAL ? channel.remoteAddress() : channel.localAddress();
        return address instanceof InetSocketAddress ? Optional.of((InetSocketAddress) address) : Optional.empty();
    }

    @Override
    public String getPath() {
        return uriDecoder.path();
    }

    @Override
    public String getQuery() {
        return uriDecoder.rawQuery();
    }

    @Override
    public HttpHeaders getHeaders() {
        return copyOf(request.headers());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Objects.toString(HttpUtil.getMimeType(request), null);
    }

    @Override
    public Charset getCharset() {
        return HttpUtil.getCharset(request, UTF_8);
    }

    @Override
    public org.zalando.logbook.HttpRequest withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public org.zalando.logbook.HttpRequest withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    void buffer(final ByteBuf content) {
        state.updateAndGet(state -> state.buffer(request, content));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
