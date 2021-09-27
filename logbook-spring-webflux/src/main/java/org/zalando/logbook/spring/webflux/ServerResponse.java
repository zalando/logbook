package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
final class ServerResponse implements HttpResponse {

    private final ServerHttpResponse response;

    private final AtomicReference<State> state = new AtomicReference<>(new State.Unbuffered());

    @Override
    public String getProtocolVersion() {
        // TODO find the real thing
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.getHeaders());
    }

    @Override
    public int getStatus() {
        return Optional.ofNullable(response.getRawStatusCode()).orElse(200);
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.getHeaders().getContentType())
                .map(MimeType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public HttpResponse withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpResponse withoutBody() {
        state.updateAndGet(State::without);
        return this;
    }

    boolean shouldBuffer() {
        return state.get() instanceof State.Offering;
    }

    void buffer(byte[] message) {
        state.updateAndGet(s -> s.buffer(message));
    }

    @Override
    public byte[] getBody() throws IOException {
        return state.get().getBody();
    }
}
