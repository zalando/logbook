package org.zalando.logbook.netty;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import lombok.AllArgsConstructor;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
final class Response implements org.zalando.logbook.HttpResponse {

    private final AtomicReference<State> state =
            new AtomicReference<>(new Unbuffered());

    private final Origin origin;
    private final HttpResponse response;

    @Override
    public String getProtocolVersion() {
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
    public Map<String, List<String>> getHeaders() {
        return response.headers().entries().stream()
                .collect(groupingBy(
                        Map.Entry::getKey,
                        mapping(Map.Entry::getValue, toList())));
    }

    @Nullable
    @Override
    public String getContentType() {
        return response.headers().get(CONTENT_TYPE);
    }

    @Override
    public Charset getCharset() {
        // TODO pick the real one
        return StandardCharsets.UTF_8;
    }

    @Override
    public org.zalando.logbook.HttpResponse withBody() {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public org.zalando.logbook.HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    void buffer(final HttpContent content) {
        state.updateAndGet(state -> state.buffer(response, content));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }

}
