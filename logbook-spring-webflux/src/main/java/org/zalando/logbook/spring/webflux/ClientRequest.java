package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;
import org.zalando.logbook.attributes.HttpAttributes;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
final class ClientRequest implements HttpRequest {

    private final org.springframework.web.reactive.function.client.ClientRequest request;

    private final AtomicReference<State> state = new AtomicReference<>(new State.Unbuffered());

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getScheme() {
        return Optional.of(request.url())
                .map(URI::getScheme)
                .orElse("");
    }

    @Override
    public String getHost() {
        return Optional.of(request.url())
                .map(URI::getHost)
                .orElse("");
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(request.url().getPort())
                .filter(p -> p != -1);
    }

    @Override
    public String getPath() {
        return Optional.of(request.url())
                .map(URI::getPath)
                .orElse("");
    }

    @Override
    public String getQuery() {
        return Optional.of(request.url())
                .map(URI::getQuery)
                .orElse("");
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(request.headers());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(request.headers().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(request.headers().getContentType())
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

    @Override
    public HttpAttributes getAttributes() {
        return new HttpAttributes(request.attributes());
    }
}
