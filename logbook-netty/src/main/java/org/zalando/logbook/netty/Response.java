package org.zalando.logbook.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http2.Http2StreamChannel;
import lombok.AllArgsConstructor;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;
import org.zalando.logbook.HttpResponse;

import jakarta.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

@AllArgsConstructor
final class Response
        implements HttpResponse, HeaderSupport {

    private final AtomicReference<State> state =
            new AtomicReference<>(new Unbuffered());

    private final ChannelHandlerContext context;
    private final Origin origin;
    private final io.netty.handler.codec.http.HttpResponse response;

    @Override
    public String getProtocolVersion() {
        if (context.channel() instanceof Http2StreamChannel) {
            return "HTTP/2.0";
        }
        return response.protocolVersion().text();
    }

    @Override
    public Origin getOrigin() {
        return origin;
    }

    @Override
    public int getStatus() {
        return response.status().code();
    }

    @Override
    public HttpHeaders getHeaders() {
        final var raw =
                SyntheticHttp2Headers.stripIfHttp2Stream(context.channel(), response.headers().copy());
        return toHeaders(raw);
    }

    @Nullable
    @Override
    public String getContentType() {
        return response.headers().get(CONTENT_TYPE);
    }

    @Override
    public Charset getCharset() {
        return HttpUtil.getCharset(response, StandardCharsets.UTF_8);
    }

    @Override
    public HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    void buffer(final ByteBuf content) {
        state.updateAndGet(state -> state.buffer(response, content));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
