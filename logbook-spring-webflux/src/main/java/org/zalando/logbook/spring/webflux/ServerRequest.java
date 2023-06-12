package org.zalando.logbook.spring.webflux;


import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
final class ServerRequest implements HttpRequest {

    private final ServerHttpRequest request;

    private final AtomicReference<State> state = new AtomicReference<>(new State.Unbuffered());

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(request.getHeaders());
    }

    @Override
    public String getRemote() {
        return Optional.ofNullable(request.getRemoteAddress()).map(InetSocketAddress::toString).orElse("");
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getScheme() {
        return request.getURI().getScheme();
    }

    @Override
    public String getHost() {
        return request.getURI().getHost();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.getURI().getPort())
                .filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return request.getPath().value();
    }

    @Override
    public String getQuery() {
        return ofNullable(request.getURI().getQuery()).orElse("");
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(request.getHeaders().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(request.getHeaders().getContentType())
                .map(MimeType::getCharset)
                .orElse(UTF_8);
    }

    @Override
    public HttpRequest withBody() throws IOException {
        state.updateAndGet(State::with);
        return this;
    }

    @Override
    public HttpRequest withoutBody() {
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
