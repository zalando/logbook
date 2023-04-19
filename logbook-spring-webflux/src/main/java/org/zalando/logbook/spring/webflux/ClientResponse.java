package org.zalando.logbook.spring.webflux;

import lombok.AllArgsConstructor;
import org.springframework.util.MimeType;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.Origin;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
final class ClientResponse implements HttpResponse {

    private final org.springframework.web.reactive.function.client.ClientResponse response;

    private final AtomicReference<State> state = new AtomicReference<>(new State.Unbuffered());

    @Override
    public int getStatus() {
        return response.statusCode().value();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders getHeaders() {
        return HttpHeaders.of(response.headers().asHttpHeaders());
    }

    @Nullable
    @Override
    public String getContentType() {
        return Optional.ofNullable(response.headers().asHttpHeaders().getContentType())
                .map(MimeType::toString)
                .orElse(null);
    }

    @Override
    public Charset getCharset() {
        return Optional.ofNullable(response.headers().asHttpHeaders().getContentType())
                .map(MimeType::getCharset)
                .orElse(UTF_8);
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

    boolean shouldBuffer() {
        return state.get() instanceof State.Offering;
    }

    void buffer(byte[] message) {
        state.updateAndGet(s -> s.buffer(message));
    }

    @Override
    public byte[] getBody() {
        return state.get().getBody();
    }
}
