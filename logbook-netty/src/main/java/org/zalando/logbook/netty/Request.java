package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor(access = PRIVATE)
final class Request implements org.zalando.logbook.HttpRequest, HeaderSupport {

    private final AtomicReference<State> state =
            new AtomicReference<>(new Unbuffered());

    private final ChannelHandlerContext context;
    private final Origin origin;
    private final HttpRequest request;
    private final URI uri;

    public Request(
            final ChannelHandlerContext context,
            final Origin origin,
            final HttpRequest request) {
        this(context, origin, request, URI.create(request.uri()));
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
        // TODO pick the real one
        return "http";
    }

    @Override
    public String getHost() {
        return request.headers().get(HOST, "unknown");
    }

    @Override
    public Optional<Integer> getPort() {
        // TODO implement
        return Optional.empty();
    }

    @Override
    public String getPath() {
        return uri.getPath();
    }

    @Override
    public String getQuery() {
        return Optional.ofNullable(uri.getQuery()).orElse("");
    }

    @Override
    public HttpHeaders getHeaders() {
        return copyOf(request.headers());
    }

    @Nullable
    @Override
    public String getContentType() {
        return request.headers().get(CONTENT_TYPE);
    }

    @Override
    public Charset getCharset() {
        // TODO pick the real one
        return StandardCharsets.UTF_8;
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
